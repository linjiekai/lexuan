package com.zhuanbo.shop.api.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.constants.UserSignedEnum;
import com.zhuanbo.core.dto.WxTokenCoreDTO;
import com.zhuanbo.core.entity.Cart;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.entity.NotifyMsg;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.Storage;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBindThird;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.core.entity.UserPartner;
import com.zhuanbo.core.entity.UserSecurityCenter;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.storage.StorageService;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.service.service.IAuthService;
import com.zhuanbo.service.service.ICartService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.INotifyMsgService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IStorageService;
import com.zhuanbo.service.service.IUserBindThirdService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserPartnerService;
import com.zhuanbo.service.service.IUserSecurityCenterService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.BaseInfoUserVO;
import com.zhuanbo.service.vo.RealNameVO;
import com.zhuanbo.service.vo.UserLoginVO;
import com.zhuanbo.service.vo.UserRedisVO;
import com.zhuanbo.shop.api.dto.req.RealNameDTO;
import com.zhuanbo.shop.api.dto.req.UserDTO;
import com.zhuanbo.shop.api.dto.resp.WxTokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/shop/mobile/user")
@Slf4j
@RefreshScope
public class MobileUserController {

    public static final String IMAGE = "image";
    @Value("${serverPhone}")
    private String serverPhone;

    @Autowired
    private IUserService userService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private IStorageService iStorageService;
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private IUserBindThirdService iShopUserBindThirdService;
    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserSecurityCenterService iShopUserSecurityCenterService;
    @Autowired
    private ICartService iCartService;
    @Autowired
    private INotifyMsgService iNotifyMsgService;
    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private QueueConfig queueConfig;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IAuthService iAuthService;
    @Autowired
    private IUserPartnerService iUserPartnerService;


    /**
     * 查
     *
     * @param userId
     * @return
     */
    @PostMapping("/info")
    public Object detail(@LoginUser Long userId, HttpServletRequest request) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseUtil.badResult();
        } else {
            UserLoginVO userLoginVO = userService.packageUser(user, false);
            userLoginVO.setUserToken(request.getHeader(Constants.LOGIN_TOKEN_KEY));
            return ResponseUtil.ok(userLoginVO);
        }
    }

    /**
     * 上传头像 与聊吧一致，用数组
     *
     * @param files
     * @return
     */
    @Transactional
    @PostMapping("/head/upload")
    public Object handleFileUpload(@LoginUser Long userId, @RequestParam("files") MultipartFile[] files, HttpServletRequest request) {

        if (files == null || files[0] == null) {
            return ResponseUtil.fail("11111", "缺少参数file");
        }
        MultipartFile file = files[0];// 只用第一个
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseUtil.badResult();
        }
        String originalFilename = file.getOriginalFilename();
        String key = generateKey(originalFilename);
        storageService.store(file, key,false);

        String url = storageService.generateUrl(key);
        Storage storageInfo = new Storage();
        storageInfo.setName(originalFilename);
        storageInfo.setSize((int) file.getSize());
        storageInfo.setType(file.getContentType());
        storageInfo.setAddTime(LocalDateTime.now());
        storageInfo.setModified(LocalDateTime.now());
        storageInfo.setStorageKey(key);
        storageInfo.setUrl(url);

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        try {

            iStorageService.save(storageInfo);
            user.setHeadImgUrl(storageInfo.getUrl());
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);
            //推送分润
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
        } catch (Exception ex) {
            txManager.rollback(status);
        }

        UserLoginVO userLoginVO = userService.packageUser(user, false);
        userLoginVO.setUserToken(request.getHeader(Constants.LOGIN_TOKEN_KEY));
        return ResponseUtil.ok(userLoginVO);
    }

    /**
     * 修改个人详细信息
     *
     * @param userId
     * @param user
     * @return
     */
    @Transactional
    @PostMapping("/info/update")
    public Object updateInfo(@LoginUser Long userId, @RequestBody User user, HttpServletRequest request) {

        User u = userService.getById(userId);
        if (u == null) {
            return ResponseUtil.badResult();
        }
        if (StringUtils.isNotBlank(user.getNickname())) {
            u.setNickname(user.getNickname());
        }
        if (u.getNickname().length() > 16) {
            return ResponseUtil.result(10043);
        }
        u.setUpdateTime(LocalDateTime.now());
        userService.updateById(u);
        
        UserLoginVO userLoginVO = userService.packageUser(u, false);
        userLoginVO.setUserToken(request.getHeader(Constants.LOGIN_TOKEN_KEY));
        
        Map<String, Object> mqMsg = MapUtil.of("mercId", authConfig.getMercId(), "userId", u.getId(),
                "mobile", u.getMobile(), "email", u.getEmail(), "type", "UPDATE", "nickname", u.getNickname());
        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), JSON.toJSONString(mqMsg));
        // live
        iRabbitMQSenderService.send(RabbitMQSenderImpl.LIVE_USER_ADD, u);
        // 同步分润系统
        iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, u);
        return ResponseUtil.ok(userLoginVO);
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
     * 设置密码
     *
     * @param user
     * @return
     */
    @PostMapping("/psd/modify")
    public Object setPs(@RequestBody User user, HttpServletRequest request) {

        User u = userService.getOne(new QueryWrapper<User>().eq("user_name", user.getMobile()));
        if (u == null) {
            return ResponseUtil.result(10007);
        }

        if (!Integer.valueOf(ConstantsEnum.USER_STATUS_1.value().toString()).equals(u.getStatus())) {
            return ResponseUtil.result(ConstantsEnum.USER_STATUS_0.value().toString().equals(u.getStatus()) ? 10032 : 10033);
        }

        String hexPassword;
        try {
            hexPassword = DigestUtils.sha1Hex(user.getPassword().getBytes("UTF-16LE"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        u.setPassword(hexPassword);
        u.setUpdateTime(LocalDateTime.now());
        userService.updateById(u);
//        BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
//        u.setPassword(bc.encode(user.getPassword()));
//        u.setUpdateTime(LocalDateTime.now());
//        userService.updateById(u);

        
        UserLoginVO userLoginVO = null;
        
        String userToken = request.getHeader(Constants.LOGIN_TOKEN_KEY);
        if (StringUtils.isBlank(userToken)) {// 登录页面设置密码,新的token
        	userLoginVO = userService.packageUser(u, true);
        } else {// 个人中心设置密码，userToken延用
        	userLoginVO = userService.packageUser(u, false);
        	userLoginVO.setUserToken(userToken);
        }
        return ResponseUtil.ok(userLoginVO);
    }

    /**
     * 解除微信绑定
     *
     * @param userId
     * @return
     */
    @Transactional
    @PostMapping("/wx/unbind")
    public Object unBindWX(@LoginUser Long userId) {

        /*UserBindThird one = iShopUserBindThirdService.getOne(new QueryWrapper<UserBindThird>().eq("user_id", userId)
                .eq("bind_type", ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.value().toString()));
        if (one != null) {
            iShopUserBindThirdService.remove(new QueryWrapper<UserBindThird>().eq("bind_id", one.getBindId()));
        }*/
        iShopUserBindThirdService.remove(new QueryWrapper<UserBindThird>().eq("user_id", userId));
        iShopUserSecurityCenterService.update(new UserSecurityCenter(),
                new UpdateWrapper<UserSecurityCenter>().eq("id", userId).set("bind_weixin", 0));
        return ResponseUtil.ok();
    }

    /**
     * 登出
     *
     * @param userId
     * @return
     */
    @PostMapping("/logout")
    public Object logout(@LoginUser Long userId, HttpServletRequest request) {
        RedisUtil.del(request.getHeader(Constants.LOGIN_TOKEN_KEY));
        userService.removeUserCache(userId);
        return ResponseUtil.ok();
    }

    /**
     * 我的首页中心
     *
     * @return
     */
    @PostMapping("/index")
    public Object index(@LoginUser Long userId, @RequestBody UserDTO userDTO) {

        User user = userService.getById(userId);

        // 订单数量
        List<Order> orderList = iOrderService.list(new QueryWrapper<Order>().select("order_status").eq("user_id", userId));

        Map<String, Object> map = new HashMap<>();

        int waitPay = 0;
        int waitShip = 0;
        int waitReceipt = 0;
        int finish = 0;
        if (!orderList.isEmpty()) {
            for (Order o : orderList) {
                if (OrderStatus.WAIT_PAY.getId().equals(o.getOrderStatus())) {
                    waitPay++;
                } else if (OrderStatus.WAIT_SHIP.getId().equals(o.getOrderStatus())) {
                    waitShip++;
                } else if (OrderStatus.WAIT_DELIVER.getId().equals(o.getOrderStatus())) {
                    waitReceipt++;
                } else if (OrderStatus.SUCCESS.getId().equals(o.getOrderStatus())) {
                    finish++;
                }
            }
        }

        map.put("waitPay", String.valueOf(waitPay));
        map.put("waitShip", String.valueOf(waitShip));
        map.put("waitReceipt", String.valueOf(waitReceipt));
        map.put("finish", String.valueOf(finish));

        int inviteSurplus = 0;
        // 暂时没了
        /*if (user.getPtLevel() == PtLevelType.PLUS.getId() && user.getPtFormal() == 0) {
        	inviteSurplus = iDictionaryService.findForLong(ConstantsEnum.LEVEL_SYSTEM.stringValue(), ConstantsEnum.DEPOSIT_UPGRADE_DA_NUMBER.stringValue()).intValue();
        	List<UserInvite> list = iUserInviteService.list(new QueryWrapper<UserInvite>().select("id").eq("pid", userId));
    		
    		if (null != list && list.size() > 0) {
    			List<Long> userIds = list.stream().map(ui -> ui.getId()).collect(Collectors.toList());
        		int inviteSurplus2 = userService.count(new QueryWrapper<User>().eq("status", 1).eq("deleted", 0).eq("pt_level", PtLevelType.PLUS.getId()).in("id", userIds));
        		inviteSurplus = inviteSurplus2 >= inviteSurplus ? 0 : inviteSurplus - inviteSurplus2;
    		}
        }*/
        // 购物车数量
        int cartNumber = iCartService.count(new QueryWrapper<Cart>().eq("user_id", userId).eq("deleted", ConstantsEnum.DELETED_0.integerValue()).eq("checked", 0));
        Map<String, Object> backMap = new HashMap<>();
        backMap.put("serverPhone", serverPhone);
        backMap.put("order", map);
        backMap.put("cartNumber", cartNumber);
        backMap.put("inviteSurplus", inviteSurplus);
        int ptLevel = 0;
        String ptNo = "";
        if (user != null) {
            ptLevel = user.getPtLevel();
            ptNo = user.getPtNo();
        }
        backMap.put("ptLevel", ptLevel);
        backMap.put("ptNo", ptNo);
        backMap.put("notifyNumber", iNotifyMsgService.count(new QueryWrapper<NotifyMsg>()
                .eq("user_id", userId).eq("read_flag", 0).eq("platform", userDTO.getPlatform())));

        Integer inviteCardVersion = userDTO.getInviteCardVersion();
        List<Dictionary> dictionaryList = iDictionaryService.findByCategoryCache(IMAGE);
        Map<String, String> dictionaryMap = dictionaryList.stream().collect(Collectors.toMap(Dictionary::getName, Dictionary::getStrVal));

        backMap.put("inviteCardStar",  dictionaryMap.getOrDefault(userDTO.getPlatform().toLowerCase() + "_inviteCardStar_" + inviteCardVersion, ""));// m星人
        backMap.put("inviteCardPlus", dictionaryMap.getOrDefault(userDTO.getPlatform().toLowerCase() + "_inviteCardPlus_" + inviteCardVersion, ""));// m达人
        backMap.put("inviteCardTrain", dictionaryMap.getOrDefault( userDTO.getPlatform().toLowerCase() + "_inviteCardTrain_" + inviteCardVersion, ""));// m体验官
        backMap.put("inviteCardPartner", dictionaryMap.getOrDefault(  userDTO.getPlatform().toLowerCase() + "_inviteCardPartner_" + inviteCardVersion, ""));// m司令合伙人
        // 邀请好友
        backMap.put("inviteFriendImage", dictionaryMap.getOrDefault(  "inviteFriendImage", ""));
        // 邀请服务商
        backMap.put("inviteServiceProviderImage", dictionaryMap.getOrDefault("inviteServiceProviderImage", ""));
        // 邀请vip
        backMap.put("inviteVipImage", dictionaryMap.getOrDefault("inviteVipImage", ""));
        // 邀请店长
        backMap.put("inviteDianZhangImage", dictionaryMap.getOrDefault("inviteDianZhangImage", ""));
        // 邀请总监
        backMap.put("inviteZongjianImage", dictionaryMap.getOrDefault("inviteZongjianImage", ""));
        // 邀请合伙人
        backMap.put("inviteHehuorenImage", dictionaryMap.getOrDefault("inviteHehuorenImage", ""));
        // 邀请联创
        backMap.put("inviteLianChuangImage", dictionaryMap.getOrDefault("inviteLianChuangImage", ""));
        return ResponseUtil.ok(backMap);
    }

    /**
     * 微信绑定
     *
     * @param userId
     * @param wxTokenDTO
     * @return
     */
    @Transactional
    @PostMapping("/wx/bind")
    public Object bindWx(@LoginUser Long userId, @RequestBody WxTokenDTO wxTokenDTO, HttpServletRequest request) throws Exception {
        AppIdKeyDTO appIdKeyDTO = new AppIdKeyDTO();
        BeanUtils.copyProperties(wxTokenDTO, appIdKeyDTO);

        WxTokenCoreDTO wxTokenCoreDTO = new WxTokenCoreDTO();
        BeanUtils.copyProperties(wxTokenDTO, wxTokenCoreDTO);
        return ResponseUtil.ok(iAuthService.userWxBind(request, userId, wxTokenCoreDTO, appIdKeyDTO));
    }

    /**
     * 我的首页中心
     *
     * @return
     */
    @PostMapping("/team")
    public Object team(@LoginUser Long userId, @RequestBody UserDTO dto) {

        return null;
    }

    /**
     * 根据邀请码获取用户信息(一点信息就行了)
     *
     * @return
     */
    @PostMapping("/info/inviteCode")
    public Object infoByInviteCode(@RequestBody User user) {
        user = userService.getOne(new QueryWrapper<User>().eq("invite_code", user.getInviteCode()));
        if (user == null) {
            return ResponseUtil.badResult();
        } else {
            Map<String, Object> backMap = new HashMap<>();
            backMap.put("nickname", user.getNickname());
            backMap.put("headImgUrl", user.getHeadImgUrl());
            backMap.put("ptLevel", user.getPtLevel());
            backMap.put("ptNo", user.getPtNo());
            return ResponseUtil.ok(backMap);
        }
    }
    
    /**
     * 实名认证信息
     * @return
     */
    @PostMapping("/real/name/info")
    public Object realNameInfo(@LoginUser Long userId) {
    	User user = userService.getOne(new QueryWrapper<User>().select("name", "card_type", "card_no", "card_no_abbr", "realed").eq("id", userId));
        return ResponseUtil.ok(user);
    }
    
    /**
     * 实名认证
     * @param realNameDTO
     * @return
     */
    @PostMapping("/real/name")
    public Object realName(@LoginUser Long userId, @RequestBody RealNameDTO realNameDTO) {

        Map<String, Object> map = new ObjectMapper().convertValue(realNameDTO, Map.class);
        map.put("userId", userId);
        map.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        RealNameVO realNameVO = userService.realName(map);

        Map<String, Object> backMap = new HashMap<>();
        backMap.put("name", realNameVO.getName());
        backMap.put("cardNoAbbr", realNameVO.getCardNoAbbr());
        return ResponseUtil.ok(backMap);
    }
    
    /**
     * 实名认证补录
     * @param realNameDTO
     * @return
     */
    @PostMapping("/real/name/repair")
    public Object realNameRepair(@LoginUser Long userId, @RequestBody RealNameDTO realNameDTO) {

    	User user = userService.getById(userId);
    	realNameDTO.setCardNo(user.getCardNo());
    	realNameDTO.setName(user.getName());
        Map<String, Object> map = new ObjectMapper().convertValue(realNameDTO, Map.class);
        map.put("userId", userId);
        map.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        RealNameVO realNameVO = userService.realName(map);

        Map<String, Object> backMap = new HashMap<>();
        backMap.put("name", realNameVO.getName());
        backMap.put("cardNoAbbr", realNameVO.getCardNoAbbr());
        return ResponseUtil.ok(backMap);
    }

    /**
     * 加盟合作协议签名
     *
     * @param userId
     * @param userDTO
     * @return
     */
    @PostMapping("/agreement/sign")
    public Object agreementSign(@LoginUser Long userId, @RequestBody UserDTO userDTO){
        log.info("|加盟合作协议签名|签名用户:{},请求参数:{}", userId, userDTO);
        User user = userService.getById(userId);
        user.setSigned(UserSignedEnum.YES.getId());
        user.setSignImgUrl(userDTO.getSignImgUrl());
        user.setSignTime(LocalDateTime.now());
        boolean signFlag = userService.updateById(user);
        if (!signFlag) {
            return ResponseUtil.fail();
        }
        UserRedisVO userRedisVO = new UserRedisVO();
        BeanUtils.copyProperties(user, userRedisVO);
        RedisUtil.set(ConstantsEnum.REDIS_USER_MPMALL.stringValue() + userId, userRedisVO, 300);
        return ResponseUtil.ok();
    }

    /**
     * 获取用户协议签名信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/get/agreement/sign")
    public Object getAgreementSign(@LoginUser Long userId){
        log.info("|加盟合作协议签名|获取签名信息:{},请求参数:{}", userId);
        User user = userService.getOne(new QueryWrapper<User>().eq("id", userId));
        Integer signed = user.getSigned();
        if (UserSignedEnum.NO.getId() == signed) {
            log.error("|加盟合作协议签名|获取签名信息|当前用户未进行协议签名");
            throw new ShopException(10054);
        }
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("signImgUrl", user.getSignImgUrl());
        userMap.put("signTime", user.getSignTime());
        return ResponseUtil.ok(userMap);
    }

    /**
     * 基本信息，php
     * @param u
     * @return
     */
    @PostMapping("/base/info")
    public Object baseInfo(@RequestBody User u) {

    	QueryWrapper<User> query = new QueryWrapper<User>();

    	if (null != u.getId()) {
    		query.eq("id", u.getId());
    	}
    	if (StringUtils.isNotBlank(u.getInviteCode())) {
    		query.eq("invite_code", u.getInviteCode());
    	}

    	User user = userService.getOne(query);

        if (user == null) {
            return ResponseUtil.badResult();
        }
        BaseInfoUserVO baseInfoUserVO = new BaseInfoUserVO();
        BeanUtils.copyProperties(user, baseInfoUserVO);
        UserInvite userInvite = iUserInviteService.getById(u.getId());
        if (userInvite != null) {
            baseInfoUserVO.setInviteUserId(userInvite.getPid());
        } else {
            baseInfoUserVO.setInviteUserId(0L);
        }

        //授权
        UserPartner userPartner = iUserPartnerService.getById(u.getId());
        if (userPartner != null) {
            baseInfoUserVO.setAuthNo(userPartner.getAuthNo());
        }

        return baseInfoUserVO;
    }
}
