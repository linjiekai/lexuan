package com.zhuanbo.shop.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.dto.CodeParamsDTO;
import com.zhuanbo.core.entity.OrderShip;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBindThird;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.core.entity.UserPartner;
import com.zhuanbo.core.sms.config.SMSParams;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.external.service.wx.service.impl.IWeixinThirdServiceImpl;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IOrderShipService;
import com.zhuanbo.service.service.IUserBindThirdService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserPartnerService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 初始化数据
 */
@RestController
@RequestMapping("/init")
@Slf4j
public class InitController {

    public static final String MP_ZHUANBO_OPEN_ID = "mp:zhuanbo:openId";
    private final String MODIFY_INVITE_CODE = "has_mp_modify_invite_code";
    @Autowired
    private IUserService iUserService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private QueueConfig queueConfig;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IUserPartnerService iUserPartnerService;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private IUserBindThirdService iShopUserBindThirdService;
    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IOrderShipService iOrderShipService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private SMSParams smsParams;

    @GetMapping("/test/redis")
    public Object testRedis(Long id, String key) {
        if (!"ssfsfsf".equals(key)) {
            return 0;
        }
        RedisUtil.set(id + "", id);
        return 1;
    }

    @GetMapping("/iosfioiurgurkcortju")
    public Object iosfioiurgurkcortju(String sfjoww) throws Exception {
        if ("WEsjrowollflwjow".equals(sfjoww)) {
//            iUserIncomeService.statTotalSaleAll(DateUtil.date10(DateUtil.beforeDay(1)));
            return 1;
        }
        return 0;
    }

    @GetMapping("/notify/live/user")
    public Object notifyLiveUser(String sfjoww) throws Exception {

    	List<User> userList = iUserService.list(new QueryWrapper<User>());

    	int index = 0;
        for (User user : userList) {
        	try {
            	//同步user数据到live
            	log.info("同步用户数据到live:[{}]", user.getId());
        		// 同步支付系统
                iRabbitMQSenderService.send(RabbitMQSenderImpl.LIVE_USER_ADD, user);
            } catch (Exception e) {
                log.error("同步用户数据live直播失败:{}", e);
                index++;
            }
        }

        return index;
    }

    /**
     * 初始化邀请码数据
     * @return
     */
    /*@GetMapping("/inviteCodeCache")
    public Object inviteCodeCache(){
        // 没有记录，从100000L开始
        if (RedisUtil.get(ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue()) == null) {
            RedisUtil.set(ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue(), 100000L);
            iUserService.checkInviteCodeNumber();
            return 0;
        }
        return 1;
    }*/

    /**
     * 刷旧的邀请码成为8位数字
     * @return
     */
    /*@GetMapping("/modifyInviteCode")
    public Object modifyInviteCode(String code){
        if (!"mpm862719".equalsIgnoreCase(code)) {
            return 0;
        }
        *//*if(redisTemplate.hasKey(MODIFY_INVITE_CODE)) {
            return 0;
        }*//*
        StringBuffer stringBuffer = new StringBuffer("");
        redisTemplate.opsForValue().set(MODIFY_INVITE_CODE, 1);
        IPage<User> page = iUserService.page(new Page<>(0, 1000), null);
        List<User> records = page.getRecords();
        stringBuffer.append(records.size());
        if (!records.isEmpty()) {
            String reg = "^[0-9]*$";
            for (User record : records) {
                if (record.getInviteCode() != null && !record.getInviteCode().matches(reg)) {
                    if (record.getPtLevel() > 0) {
                        record.setInviteCode(iUserService.generateInviteCode());
                        stringBuffer.append(",").append(iUserService.updateById(record));
                    }
                }
            }
        }
        return stringBuffer.toString();
    }*/

    /**
     * 同步用户信息(支付)
     * @return
     */
    @GetMapping("/sysu")
    public Object sysu(){
        List<User> list = iUserService.list(null);
        Map<String, Object> mqMsg;
        for (User user : list) {
            mqMsg = MapUtil.of("mercId", authConfig.getMercId(), "userId", user.getId(), "mobile", user.getMobile(),
                    "email", user.getEmail(), "type", "ADD", "nickname", user.getNickname());
            rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), JSON.toJSONString(mqMsg));
        }
        return 0;
    }


    @GetMapping("rmRedisById")
    public Object rmRedisById(Long id){
        iUserService.removeUserCache(id);
        return 0;
    }
    @GetMapping("/byId")
    public Object byId(Long id){
        User user = iUserService.getById(id);
        UserPartner userPartner = iUserPartnerService.getById(user.getId());

        UserLoginVO userLoginVo = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVo);
        userLoginVo.setAuthNo(userPartner.getAuthNo());
        userLoginVo.setAuthDate(userPartner.getAuthDate());
        userLoginVo.setTeamName(userPartner.getTeamName());
        if (false) {
            userLoginVo.setUserToken(CharUtil.getRandomString(32));
            RedisUtil.set(userLoginVo.getUserToken(), user.getId(), 3600 * 24 * 30);
        }

//        String cardNoAbbr = null;
//        if (!StringUtils.isBlank(user.getCardNo())) {
//            String key = dictionaryService.findForString("SecretKey", "AES");
//
//            try {
//                String cardNo = new String (AESCoder.decrypt(Base64.decodeBase64(user.getCardNo()), Base64.decodeBase64(key)));
//                cardNoAbbr = cardNo.substring(0, 3) + "******" + cardNo.substring(cardNo.length() - 4, cardNo.length());
//                userLoginVo.setCardNoAbbr(cardNoAbbr);
//            } catch (Exception e) {
//                log.error("userId={},cardNo={}, 解密失败", user.getId(), user.getCardNo());
//            }
//        }

        // openId and token
        List<UserBindThird> userBindThirdList = iShopUserBindThirdService.list(new QueryWrapper<UserBindThird>().eq("user_id", user.getId()));
        if (!userBindThirdList.isEmpty()) {
            for (UserBindThird ub : userBindThirdList) {
                if (ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.stringValue().equals(ub.getBindType())) {
                    userLoginVo.setOpenId(ub.getOpenId());
                    userLoginVo.setWxName(ub.getNickname());
                    break;
                }
            }
        }
        if (StringUtils.isNotBlank(user.getPassword())) {
            userLoginVo.setHasPwd(1);
        }
        Object o = RedisUtil.get(MP_ZHUANBO_OPEN_ID);// 小程序openId
        if (o != null) {
            userLoginVo.setWxOpenId(String.valueOf(o));
        }
        return userLoginVo;
    }

    @Transactional
    @GetMapping("/refreshShip")
    public Object refreshShip(String key, Long uid, Long n, Long o) throws Exception {
        if (!"fjpoojooaf".equals(key)) {
            return 0;
        }
        iUserInviteService.update(new UserInvite(), new UpdateWrapper<UserInvite>().set("pid", n).eq("id", uid));
        return 1;
    }

    @GetMapping("/mqsend")
    public Object mqsend(String key, String orderNo){
        if (!"hjroifjshfjois".equals(key)) {
            return 0;
        }
        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getOrder().getRoutingKey(), JSON.toJSONString(MapUtil.of("orderNo", orderNo)), message -> {
            message.getMessageProperties().setHeader(ConstantsEnum.MQ_RETRY_TIME.stringValue(), 0);
            return message;
        });
        return 1;
    }

    @GetMapping("/ship/trace")
    public Object shipTrace(String orderNo){

        List<Object> result = new ArrayList<>();

        QueryWrapper<OrderShip> orderShipQueryWrapper = new QueryWrapper<>();
        orderShipQueryWrapper.eq("order_no", orderNo);
        /*orderShipQueryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        orderShipQueryWrapper.eq("ship_sn", orderParamsDTO.getShipSn());*/

        OrderShip orderShip1 = new OrderShip();
        orderShip1.setOrderNo("2019052700000037");
        orderShip1.setShipChannel("BTKD");
        orderShip1.setShipSn("71693704124423");

        List<OrderShip> orderShipList = Arrays.asList(orderShip1);//null;iOrderShipService.list(orderShipQueryWrapper);
        if (!orderShipList.isEmpty()) {

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + authConfig.getShipAppCode());
            String query;
            List<JSONObject> detailList;

            for (OrderShip orderShip : orderShipList) {

                if (!OrderStatus.SUCCESS.getId().equalsIgnoreCase(orderShip.getOrderStatus())) {
                    query = HttpUtil.sendGet(authConfig.getShipTraceUrl(), "comid=" + orderShip.getShipChannel()
                            + "&number=" + orderShip.getShipSn(), headers);
                    detailList = iOrderShipService.toDetailList(query);
                    if (detailList.isEmpty()) {
                        continue;
                    }
                    if (iOrderShipService.isSuccessbyDetail(detailList)) {
                        // 更新物流数据
                        orderShip.setOrderStatus(OrderStatus.SUCCESS.getId());
                        orderShip.setRouteInfo(JSON.toJSONString(detailList));
                        orderShip.setUpdateTime(LocalDateTime.now());
                        iOrderShipService.updateById(orderShip);
                    }
                    result.addAll(detailList);
                } else {
                    result.addAll(JSON.parseArray(orderShip.getRouteInfo()));
                }
            }
        }
        return ResponseUtil.ok(result);
    }

    /**
     * 同步arangodb
     * @param key
     * @return
     */
    @GetMapping("/arangodb")
    public Object initArangodb(String key, Integer type) {
        if (!"JSFJO3u5pefs".equals(key)) {
            return 0;
        }
        // 一口气吧
        if (type.equals(0)) {// 用户
            List<User> list = iUserService.list(new QueryWrapper<User>().select("id"));
            for (User user : list) {
                iRabbitMQSenderService.send(RabbitMQSenderImpl.G_USER, user);
            }
        } else {// 关系
            /*List<UserInvite> list = iUserInviteService.list(new QueryWrapper<UserInvite>().select("id,pid"));
            Map<String, Object> of;
            for (UserInvite userInvite : list) {
                of = MapUtil.of("type", GraphServiceImpl.ACTION_UF, "data",
                        MapUtil.of("tid", userInvite.getId(), "fid", userInvite.getPid()));
                iRabbitMQSenderService.send(RabbitMQSenderImpl.G_USER_OF, of);
            }*/
        }
        return 1;
    }

    /**
     * 查看团队成员
     * @param id
     * @return
     * @throws Exception
     */
    @GetMapping("/teams")
    public Object teams(Long id) throws Exception {
        return iUserInviteService.getTeamFilterId(id);
    }

    @GetMapping("/cc")
    public Object cc(){
        int page = 1;
        int limit = 100;
        ArrayList<Object> objects = new ArrayList<>();
        IPage<User> page1 = iUserService.page(new Page<User>(page, limit), null);
        while (page1.getRecords().size() > 0) {
            for (User record : page1.getRecords()) {
                Object testcc = iUserService.testcc(record.getId());
                if (testcc != null) {
                    objects.add(record.getId() + ":" + testcc);
                }
            }
            page++;
            page1 = iUserService.page(new Page<User>(page, limit), null);
        }
        return objects;
    }

    @GetMapping("/rmmpaccesstoken")
    public Object rmmpaccesstoken(String key){
        if (!"lsfjlshefws".equals(key)) {
            return 0;
        }
        RedisUtil.del(IWeixinThirdServiceImpl.MP_MPAMLL_ACCESSTOKEN);
        return 1;
    }

    @PostMapping("/test")
    public Object test(@RequestBody CodeParamsDTO codeParamsDTO){
        System.out.println(codeParamsDTO);
        return 1;
    }


    @GetMapping("/inviteCode")
    public Object inviteCode(String key) {
        if (!"soijf0j".equals(key)) {
            return 0;
        }
        String s = iUserService.generateInviteCode();
        return s + "";
    }

    @GetMapping("/inviteCodeMake")
    public Object inviteCodeMake(String key) {
        if (!"soijf0j".equals(key)) {
            return 0;
        }
        iUserService.checkInviteCodeNumber();
        return 1;
    }

    /**
     * 重置邀请码
     * @param key
     * @return
     */
    @GetMapping("/clearinvitecode")
    public Object clearInviteCode(String key){
        if (!"iokr8tku".equals(key)) {
            return 0;
        }
        Long[] l = new Long[0];
        RedisUtil.del(ConstantsEnum.REDIS_INVITE_CODE_SET.stringValue());
        RedisUtil.sSet(ConstantsEnum.REDIS_INVITE_CODE_SET.stringValue(), l);
        // redis
        //RedisUtil.set(ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue(), 100000L);
        RedisUtil.del(ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue());
        iUserService.checkInviteCodeNumber();
        return 1;
    }

    /**
     * 刷新邀请码所有
     * @param key
     * @return
     */
    @GetMapping("/refreshinvitecode")
    public Object refreshinvitecode(String key){
        if (!"iokr8tsf64sfku".equals(key)) {
            return 0;
        }
        List<User> list = iUserService.list(null);
        list.forEach(x -> {
            x.setInviteCode(iUserService.generateInviteCode());
            iUserService.updateById(x);
        });
        return 1;
    }
}
