package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Quick;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IQuickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 快捷入口表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/quick")
@Slf4j
public class QuickController {

    @Autowired
    private IQuickService iQuickService;
    @Autowired
    private IAdminService iAdminService;

    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_QUICK_UPDATE = "lock_quick_update_";
    /**
     * 列表
     * @param page
     * @param limit
     * @param quick
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit, Quick quick) {


        IPage<Quick> pageCond = new Page<>(page, limit);
        QueryWrapper<Quick> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("indexs");
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        queryWrapper.orderByAsc(true, "id");


        IPage<Quick> adIPage = iQuickService.page(pageCond, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        data.put("items", adIPage.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 增
     * @param quick
     * @return
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody Quick quick) {
        try {
            validateData(quick,0);
            quick.setAdminId(adminId);
            quick.setOperator(iAdminService.getAdminName(adminId));
            //保存数据
            iQuickService.save(quick);
            //返回提示
            return ResponseUtil.ok();
        } catch (Exception e) {
            log.error("QuickController create error :{}",e);
            return ResponseUtil.fail();
        }
    }

    /**
     * 查
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Long id) {
        Quick quick = iQuickService.getById(id);
        if (quick == null) {
            return ResponseUtil.badResult();
        } else {
            return ResponseUtil.ok(quick);
        }
    }

    /**
     * 修改
     * @param
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody Quick quick) {
        String s = LOCK_QUICK_UPDATE+quick.getId();
        boolean b = redissonLocker.tryLock(s, TimeUnit.SECONDS, 10, 30);
        try {
            if(!b){
                return ResponseUtil.result(30014);
            }
            validateData(quick,1);
            quick.setAdminId(adminId);
            quick.setOperator(iAdminService.getAdminName(adminId));
            quick.setUpdateTime(LocalDateTime.now());
            iQuickService.updateById(quick);
            //返回成功提示
            return ResponseUtil.ok();
        } catch (Exception e) {
            log.error("QuickController update error :{}",e);
            return ResponseUtil.serious();
        }finally {
            if(b){
                redissonLocker.unlock(s);
            }

        }
    }

    /**
     * @Description(描述): 检验数据
     * @auther: Jack Lin
     * @param :[quick, type]
     * @return :void
     * @date: 2019/7/15 11:51
     */
    private void validateData(Quick quick, int type) {
        List<Quick> list = iQuickService.list(new QueryWrapper<Quick>().eq("level", quick.getLevel()).eq("deleted", ConstantsEnum.DELETED_0.integerValue()).eq("indexs", quick.getIndexs()));
        if(!CollectionUtils.isEmpty(list)){
            //创建的直接抛错
            if(0==type){
                throw  new ShopException(71007);
            }
            for (Iterator<Quick> i = list.iterator(); i.hasNext() ; ) {
                Quick quick1 = i.next();
                //需要除去自身
                if(quick1.getId().longValue()==quick.getId().longValue()){continue;}
                //如果还有则抛错
                throw  new ShopException(71007);
            }
        }
    }


    /**
     * 删除
     * @param
     * @return
     */
    @PostMapping("/delete")
    public Object delete(@LoginAdmin Integer adminId, @RequestBody Quick quick) {
        String s = LOCK_QUICK_UPDATE+quick.getId();
        boolean b = redissonLocker.tryLock(s, TimeUnit.SECONDS, 10, 30);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            iQuickService.update(new Quick(), new UpdateWrapper<Quick>().set("indexs",-1).set("deleted", ConstantsEnum.DELETED_1.integerValue()).eq("id", quick.getId()));
            return ResponseUtil.ok();
        }catch (Exception e){
            log.error("QuickController delete error :{}",e);
            throw e;
        }finally {
            if(b){
                redissonLocker.unlock(s);
            }
        }

    }
}
