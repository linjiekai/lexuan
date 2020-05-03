package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.UpgradeDetails;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.service.*;
import com.zhuanbo.service.utils.LogOperateUtil;
import com.zhuanbo.service.vo.UpgradeDetailsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/upgrade")
public class UpgradeDetailsController {

    @Autowired
    private IUpgradeDetailsService iUpgradeDetailsService;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IAdminService iAdminService;

    @Autowired
    private ISeqIncrService iSeqIncrService;

    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_UPGRADE_UPDATE = "lock_upgrade_update_";
    /**
     * 获取升级消费记录
     * @param page
     * @param limit
     * @param userId
     * @param mobile
     * @return
     */
    @GetMapping("list")
    public Object list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       Long userId,String mobile){
        List<UpgradeDetailsVo> upList=iUpgradeDetailsService.findUpgradeDetails((page-1)*limit,limit,userId,mobile);
        //总数
        Integer total=iUpgradeDetailsService.countUpgradeTotalRecords(userId,mobile);
        //如果获取的数据不为空
        if(upList!=null){
            //遍历数据添加直属达人数
            for(UpgradeDetailsVo udVo:upList){
                //统计用户直属达人数量
               Integer dNum= iUpgradeDetailsService.countDarenNum(udVo.getUserId());
               udVo.setDarenNum(dNum);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total",total);
        data.put("items",upList);
        return ResponseUtil.ok(data);

    }

    /**
     * 根据手机号码获取用户信息
     * @param mobile
     * @return
     */
    @GetMapping("findUserByMobile")
    public Object findUserByMobile(String mobile){
        if(mobile==null){
            mobile="";
        }
        return ResponseUtil.ok(iUpgradeDetailsService.findUserByMobile(mobile));
    }

    /**
     * 统计直属达人数
     * @param userId
     * @return
     */
    @GetMapping("countDarenNum")
    public Object countDarenNum(Long userId){
        Integer derenNum=iUpgradeDetailsService.countDarenNum(userId);
        Map<String,Object> map=new HashMap<>();
        map.put("darenNum",derenNum);
        map.put("refundFlag",0);
        return ResponseUtil.ok(map);
    }


    /**
     * 添加充值记录
     * @param adminId
     * @param upgradeDetails
     * @return
     */
    @PostMapping("create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody UpgradeDetails upgradeDetails){
        LogOperateUtil.log("充值记录", "添加充值记录", null, adminId.longValue(), 0);
        //获取用户信息
        User user=iUserService.getOne(new QueryWrapper<User>().eq("id",upgradeDetails.getUserId()));
        if(user==null){
            return ResponseUtil.fail(10007);
        }
        //如果用户身份是M体验官、M司令身份，返回提示
        if(user.getPtLevel() > 1){
            return ResponseUtil.fail(11011);
        }
        //事务
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        try{
            upgradeDetails.setOrderDate(DateUtil.LocalDateTimeToString(LocalDateTime.now(),"yyyy-MM-dd"));
            upgradeDetails.setOrderTime(DateUtil.LocalDateTimeToString(LocalDateTime.now(),"HH:mm:ss"));
            upgradeDetails.setPayTime(DateUtil.LocalDateTimeToString(LocalDateTime.now(),"HH:mm:ss"));
            upgradeDetails.setPayDate(DateUtil.LocalDateTimeToString(LocalDateTime.now(),"yyyy-MM-dd"));
            upgradeDetails.setPayType(0);
            upgradeDetails.setAdminId(adminId);
            upgradeDetails.setOperator(iAdminService.getAdminName(adminId));
            upgradeDetails.setOrderNo(DateUtil.date8() + iSeqIncrService.nextVal("order_no", 8, Align.LEFT));
            upgradeDetails.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
            //保存记录
            iUpgradeDetailsService.save(upgradeDetails);
            //押金充值快速升级(达人转体验官)
//            iUserLevelService.quickUpGrade(upgradeDetails.getUserId());
            txManager.commit(status);
            return ResponseUtil.ok();
        }catch(Exception e){
            e.printStackTrace();
            txManager.rollback(status);
            return ResponseUtil.fail();
        }

    }

    /**
     * 修改充值记录
     * @param adminId
     * @param upgradeDetails
     * @return
     */
    @Transactional
    @PostMapping("update")
    public Object update(@LoginAdmin Integer adminId,@RequestBody UpgradeDetails upgradeDetails){
        LogOperateUtil.log("充值记录", "修改充值记录", null, adminId.longValue(), 0);
         //获取用户信息
         User user=iUserService.getOne(new QueryWrapper<User>().eq("id",upgradeDetails.getUserId()));
         if(user==null){
             return ResponseUtil.fail(10007);
         }
        //如果用户身份是M体验官、M司令身份，返回提示 ？？？ 限制？？？
        /*if(user.getPtLevel() > 1){
            return ResponseUtil.fail(11011);
        }*/
        String key = LOCK_UPGRADE_UPDATE+upgradeDetails.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 30);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            //如果为退款
            if (upgradeDetails.getRefundFlag() == 1) {
                iUserService.updateById(user);
            }
            //更新记录信息
            iUpgradeDetailsService.updateById(upgradeDetails);
            return ResponseUtil.ok();
        }catch (Exception e){
            throw e;
        }finally {
            if(b){
                redissonLocker.unlock(key);
            }

        }
    }


}
