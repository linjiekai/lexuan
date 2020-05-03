package com.zhuanbo.admin.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.annotation.LoginDealersAdmin;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.dto.AdminCheckUserDTO;
import com.zhuanbo.core.dto.AdminModifyUser;
import com.zhuanbo.core.dto.AdminRealNameDTO;
import com.zhuanbo.core.dto.AdminUserDTO;
import com.zhuanbo.core.entity.Storage;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.storage.StorageService;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.bcrypt.BCryptPasswordEncoder;
import com.zhuanbo.core.vo.UserUpgradePointVO;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IStorageService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.utils.LogOperateUtil;
import com.zhuanbo.service.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/user")
@Slf4j
public class UserController {


    @Autowired
    private IUserService userService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private IStorageService iStorageService;
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private ICashService iCashService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_USER_UPDATE = "lock_user_update_";
    @Autowired
    AuthConfig authConfig;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private IDepositOrderService iDepositOrderService;

    /**
     * 列表
     *
     * @param page
     * @param limit
     * @param sort
     * @param userDTO
     * @return
     */
//    @GetMapping("/list")
//    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
//                       @RequestParam(defaultValue = "10") Integer limit,
//                       @Sort @RequestParam(defaultValue = "add_time") String sort,
//                       UserDTO userDTO) throws Exception {
//
//        Page<UserVO> pageCond = new Page<>(page, limit);
//        Map<String, Object> ew = new HashMap<>();
//        if (userDTO != null) {
//            if (StringUtils.isNotBlank(userDTO.getMobile())) {
//                ew.put("mobile", userDTO.getMobile() + "%");
//            }
//            if (userDTO.getId() != null) {
//                ew.put("id", userDTO.getId());
//            }
//            if (userDTO.getPtLevel() != null) {
//                ew.put("ptLevel", userDTO.getPtLevel());
//            }
//            if (userDTO.getPid() != null) {
//                ew.put("pid", userDTO.getPid());
//            }
//            if (userDTO.getInviteNumber() != null) {
//                ew.put("inviteNumber", userDTO.getInviteNumber());
//            }
//            if (userDTO.getListType() != null) {
//                ew.put("listType", userDTO.getListType());
//            }
//            if (StringUtils.isNotBlank(userDTO.getNickname())) {
//                ew.put("nickname", userDTO.getNickname() + "%");
//            }
//        }
//
//        IPage<UserVO> adIPage = userService.pageManual(pageCond, ew);
//        Map<String, Object> data = new HashMap<>();
//        data.put("total", adIPage.getTotal());
//
//        UserIncome userIncome = null;
//        for (UserVO record : adIPage.getRecords()) {
//            // 玩家的
//            if (userDTO.getListType() != null && userDTO.getListType().equals(1)) {
//                // 团队人数 消费金额 累计销售 累计收益 课时费 在途收益 可提收益
////                userIncome = iUserIncomeService.getUserIncome(record.getId());
//                if (userIncome != null) {
//                    record.setTotalTeam(userIncome.getTotalTeam());
//                    record.setConsumPrice(userIncome.getTotalConsume());
//                    record.setTotalSale(userIncome.getTotalSale());
//                    record.setTotalIncome(userIncome.getTotalIncome());
//                    record.setTrainIncome(userIncome.getTrainIncome());
//                    record.setTotalUavaIncome(userIncome.getTotalUavaIncome());
////                    record.setWithdIncome(iCashService.balance(record.getId()));
//                    record.setTrainIncomeBase(userIncome.getTrainIncomeBase());
//                    record.setTrainIncomePartner(userIncome.getTrainIncomePartner());
//                    record.setTotalTrainIncome(userIncome.getTrainIncomePartner().add(userIncome.getTrainIncomeBase()).add(userIncome.getTrainIncomeMp())); //累计课时费
//                    record.setTrainIncomeMp(userIncome.getTrainIncomeMp());
//                }
//            }
//        }
//        data.put("items", adIPage.getRecords());
//        return ResponseUtil.ok(data);
//    }

    /**
     * 增
     *
     * @param user
     * @param bindingResult
     * @return
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody @Valid User user, BindingResult bindingResult) {
        LogOperateUtil.log("用户管理", "创建", null, adminId.longValue(), 0);
        if (bindingResult.hasErrors()) {
            return ResponseUtil.badValidate(bindingResult);
        }
        if (StringUtils.isBlank(user.getPassword())) {
            return ResponseUtil.fail(403, "密码不能为空");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        // 手机号唯一
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", user.getMobile());
        if (userService.getOne(queryWrapper) != null) {
            return ResponseUtil.fail(-1, "手机号已存在");
        }
        user.setOperator(iAdminService.getById(adminId).getUsername());
        userService.save(user);
        return ResponseUtil.ok(user);
    }

    /**
     * 删
     *
     * @param id
     * @return
     */
    @PostMapping("/delete/{id}")
    public Object delete(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        LogOperateUtil.log("用户管理", "删除", String.valueOf(id), adminId.longValue(), 0);
        User user = userService.getById(id);
        if (user == null) {
            return ResponseUtil.badResult();
        }
        user.setStatus(2);
        String key = LOCK_USER_UPDATE + id;
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 30);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            userService.updateById(user);
            return ResponseUtil.ok();
        } catch (Exception e) {
            throw e;
        } finally {
            if (b) {
                redissonLocker.unlock(key);
            }
        }

    }

    /**
     * 查
     *
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseUtil.badResult();
        } else {
            return ResponseUtil.ok(user);
        }
    }

    /**
     * 改
     *
     * @param dto
     * @return
     * @throws Exception
     */
//    @PostMapping("/update")
//    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminUserDTO dto) throws Exception {
//        LogOperateUtil.log("用户管理", "修改", String.valueOf(dto.getId()), adminId.longValue(), 0);
//        dto.setOperatorId(adminId);
//        Map<String, Object> backMap = userService.updateUser(dto);
//        // 清除缓存
//        if (backMap != null && backMap.containsKey(ConstantsEnum.NEED_REMOVE_CACHE_IDS.toString())) {
//            List<Long> needRemoveCacheIds = (List<Long>) backMap.get(ConstantsEnum.NEED_REMOVE_CACHE_IDS.toString());
//            userService.removeUserCacheList(needRemoveCacheIds);
//        }
//        UserDTO userDTO = new UserDTO();
//        userDTO.setId(dto.getId());
//        return list(adminId, 1,
//                1, "add_time",
//                userDTO);
//    }


    /**
     * 修改密码
     *
     * @return
     */
    @PostMapping("/updatePwd")
    public Object updatePwd(@LoginAdmin Integer adminId, @RequestBody AdminUserDTO dto) throws Exception {
        LogOperateUtil.log("用户管理", "修改密码", String.valueOf(dto.getId()), adminId.longValue(), 0);
        Long userId = dto.getId();
        String password = dto.getPassword();
        if(StringUtils.isBlank(password)){
            return ResponseUtil.fail(10402,"缺少密码");
        }

        User user = userService.getById(userId);
        user.setPassword(DigestUtils.sha1Hex(password.getBytes("UTF-16LE")));
        user.setUpdateTime(LocalDateTime.now());
        user.setOperator(iAdminService.getAdminName(adminId));
        userService.updateById(user);
        userService.removeUserCache(user.getId());

        //同步支付
        iRabbitMQSenderService.send(RabbitMQSenderImpl.PAY_UPDATE, user);
        // 同步分润系统
        iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);

        return ResponseUtil.ok();
    }


    /**
     * 上传头像
     *
     * @param file
     * @return
     */
    @PostMapping("/uploadImg")
    public Object handleFileUpload(@LoginAdmin Integer userId, @RequestParam("file") MultipartFile file) throws IOException {


        if (file == null) {
            return ResponseUtil.fail(11111, "缺少参数files");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseUtil.badResult();
        }

        String originalFilename = file.getOriginalFilename();
        String key = generateKey(originalFilename);
        storageService.store(file, key, false);

        String url = storageService.generateUrl(key);
        Storage storageInfo = new Storage();
        storageInfo.setName(originalFilename);
        storageInfo.setSize((int) file.getSize());
        storageInfo.setType(file.getContentType());
        storageInfo.setAddTime(LocalDateTime.now());
        storageInfo.setModified(LocalDateTime.now());
        storageInfo.setStorageKey(key);
        storageInfo.setUrl(url);

        String lock = LOCK_USER_UPDATE + user.getId();
        boolean b = redissonLocker.tryLock(lock, TimeUnit.SECONDS, 10, 30);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            iStorageService.save(storageInfo);
            user.setHeadImgUrl(storageInfo.getUrl());
            userService.updateById(user);
        } catch (Exception ex) {
            txManager.rollback(status);
        } finally {
            if (b) {
                redissonLocker.unlock(lock);
            }

        }
        txManager.commit(status);

        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        return ResponseUtil.ok(map);
    }

    private String generateKey(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        String suffix = originalFilename.substring(index);
        String key = null;
        Storage storageInfo = null;
        do {
            key = CharUtil.getRandomString(20) + suffix;
            storageInfo = iStorageService.getOne(new QueryWrapper<Storage>().eq("storage_key", key));
        }
        while (storageInfo != null);
        // 格式：head/yyyymmdd/[hash].png
        return "head/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/" + key;
    }

    /**
     * 只返回用户id和用户名
     *
     * @return
     */
    @GetMapping("/idNameList")
    public Object idNameList(@LoginAdmin Integer adminId,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "-1") Integer limit,
                             @RequestParam(required = false) Integer id) {

        Page<User> pageCond = new Page<>(page, limit);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        IPage<User> iPage = userService.page(pageCond, queryWrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", limit.equals(-1) ? userService.count(queryWrapper) : iPage.getTotal());
        List<Map> list = new ArrayList<>();
        if (iPage.getRecords().size() > 0) {
            for (User u : iPage.getRecords()) {
                list.add(MapUtil.of("id", u.getId(), "name", u.getNickname()));
            }
        }
        map.put("item", list);
        return ResponseUtil.ok(map);
    }

    /**
     * 屏蔽
     *
     * @param adminId
     * @param user
     * @return
     */
//    @PostMapping("/shield")
//    public Object shield(@LoginAdmin Integer adminId, @RequestBody User user) throws Exception {
//        LogOperateUtil.log("用户管理", "屏蔽", String.valueOf(user.getId()), adminId.longValue(), 0);
//        if (user.getId() == null) {
//            return ResponseUtil.fail("11111", "缺少参数：id");
//        }
//        user = userService.getById(user.getId());
//        if (user == null) {
//            return ResponseUtil.result(10007);
//        }
//        int shield = 0;
//        if (user.getShield() != null && user.getShield().equals(0)) {
//            shield = 1;
//        }
//
//        userService.update(new User(), new UpdateWrapper<User>().eq("id", user.getId())
//                .set("shield", shield)
//                .set("add_time", LocalDateTime.now())
//                .set("operator", iAdminService.getAdminName(adminId)));
//        userService.removeUserCache(user.getId());
//        UserDTO u = new UserDTO();
//        u.setId(user.getId());
//        return list(adminId, 1, 1, null, u);
//    }


    /**
     * 实名用户列表
     */
    @PostMapping("/real/name/list")
    public Object realNameList(@LoginAdmin Integer adminId, @RequestBody AdminRealNameDTO dto) throws Exception {
        LogOperateUtil.log("用户管理", "实名用户列表", String.valueOf(dto.getUserId()), adminId.longValue(), 0);
        return userService.realNameList(dto);
    }

    @GetMapping("/refreshAllUserLevel")
    public Object refreshAllUserLevel() throws Exception {
//        iUserLevelService.refreshAllUserLevel();
        return ResponseUtil.ok();

    }

    /**
     * 提现银行卡列表
     */
    @PostMapping("/bank/withdr/list")
    public Object withdrBankList(@LoginAdmin Integer adminId, @RequestBody AdminUserDTO dto) throws Exception {
        log.info("|提现银行卡列表|接收到请求报文:{}", dto);
        return userService.withdrBankList(dto);
    }

    /**
     * 快捷支付银行卡列表
     */
    @PostMapping("/bank/quick/list")
    public Object quickBankList(@LoginAdmin Integer adminId, @RequestBody AdminUserDTO dto) throws Exception {
        log.info("|快捷支付银行卡列表|接收到请求报文:{}", dto);
        return userService.quickBankList(dto);
    }

    /**
     * 实名信息置为无效
     */
    @PostMapping("/cancel/realname/{id}")
    public Object cancelRealname(@LoginAdmin Integer adminId, @PathVariable("id") Long id) throws Exception {
        LogOperateUtil.log("用户管理", "实名信息置为无效", String.valueOf(id), adminId.longValue(), 0);
        return userService.cancelRealname(adminId, id);
    }


    /*@GetMapping("/userInfoByYinli")
    @UnAuthAnnotation
    @ResponseBody
    public Object userInfoByYinli(@RequestParam Long userId) throws Exception {
        User user = userService.getById(userId);
        Optional.ofNullable(user).orElseThrow(()->new ShopException(10404));
        UserDTO dto = new UserDTO();
        dto.setMobile(user.getMobile());
        yinliAdminClient.sendSms()
        String post = HttpUtil.post(YINLI_ADMIN_HOST + "/mpmall/admin/user/userInfoByInterior", JacksonUtil.objTojson(dto));
        return  post;
    }*/

    /**
     * 修改手机号(只影响登录)
     * @return
     */
    @PostMapping("/modify/mobile")
    public Object modifyMobile(@LoginAdmin Integer adminId, @RequestBody AdminModifyUser adminModifyUser) {
        // LogOperateUtil.log("用户管理", "修改手机号", String.valueOf(adminModifyUser.getUserId()), adminId.longValue(), 0);
        userService.modifyMobile(adminId, adminModifyUser);

        User user = userService.getById(adminModifyUser.getUserId());
        if (user != null) {
            iRabbitMQSenderService.send(RabbitMQSenderImpl.PAY_UPDATE, user);
            // 同步分润系统
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
        }
        return ResponseUtil.ok();
    }


    @GetMapping("/account/info")
    public Object accountInfo(@LoginAdmin Integer adminId,
                              @RequestParam Integer id) throws Exception {
        UserVO userVO = userService.accounInfo(id);
        JSONObject da = iCashService.balanceObj(id.longValue());
        userVO.setWithdrawIncome(da.getBigDecimal("acBal"));// 可提收益
        userVO.setWithdrawAlready(da.getBigDecimal("withdrBal"));//已提现总额
        return ResponseUtil.ok(userVO);
    }

    /**
     * 授权码校验
     *
     * @param adminId
     * @param adminUserDTO
     * @return
     */
    @PostMapping("/management/check/user")
    public Object managementCheckUser(@LoginDealersAdmin Integer adminId, @RequestBody AdminUserDTO adminUserDTO, HttpServletRequest request){
        log.info("|用户检测|操作人:{},接收到请求报文:{}", adminId, adminUserDTO);
        AdminCheckUserDTO checkUserDTO = userService.managementCheckUser(adminId.longValue(), adminUserDTO, request);
        return ResponseUtil.ok(checkUserDTO);
    }

    /**
     * 后台注册会员
     *
     * @param adminId
     * @param adminUserDTO
     * @return
     */
    @PostMapping("/management/create")
    public Object managementCreate(@LoginDealersAdmin Integer adminId, @RequestBody AdminUserDTO adminUserDTO, HttpServletRequest request){
        log.info("|后台注册会员|操作人:{},接收到请求报文:{}", adminId, adminUserDTO);
        Map<String, Object> profitOrderMap = userService.managementCreateUser(adminId.longValue(), adminUserDTO, request);
        // 充值订单同步
        if (profitOrderMap != null) {
            iDepositOrderService.syncDepositOrderToProfit(profitOrderMap);
        }
        return ResponseUtil.ok();
    }

    /**
     * 修改用户等级
     *
     * @param adminId
     * @param adminUserDTO
     * @return
     */
    @PostMapping("/management/edit/ptLevel")
    public Object managementEditPtlevel(@LoginDealersAdmin Integer adminId, @RequestBody AdminUserDTO adminUserDTO){
        log.info("|修改用户等级|操作人:{},接收到请求报文:{}", adminId, adminUserDTO);
        Map<String, Object> profitOrderMap = userService.managementEditPtlevel(adminId.longValue(), adminUserDTO);
        // 充值订单同步
        if (profitOrderMap != null) {
            iDepositOrderService.syncDepositOrderToProfit(profitOrderMap);
        }
        return ResponseUtil.ok();
    }

    /**
     * 修改等级,获取积分信息
     * @return
     */
    @PostMapping("/management/upgrade/point")
    public Object managementUpgradePoint(@LoginDealersAdmin Integer adminId, @RequestBody AdminUserDTO adminUserDTO){
        log.info("|后去用户升级积分|操作人:{},接收到请求报文:{}", adminId, adminUserDTO);
        UserUpgradePointVO userUpgradePointVO = userService.managementUpgradePoint(adminId.longValue(), adminUserDTO);
        return ResponseUtil.ok(userUpgradePointVO);
    }
}
