package com.zhuanbo.service.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.crypt.WxMaCryptUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.client.server.client.CommonClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.client.server.dto.common.SmsDTO;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.CopyWritingConstants;
import com.zhuanbo.core.constants.DepositOrderBusiTypeEnum;
import com.zhuanbo.core.constants.DepositOrderTypeSplitEnum;
import com.zhuanbo.core.constants.MQDataTypeEnum;
import com.zhuanbo.core.constants.MapKeyEnum;
import com.zhuanbo.core.constants.PayInterfaceEnum;
import com.zhuanbo.core.constants.PointTypeEnum;
import com.zhuanbo.core.constants.PtLevelType;
import com.zhuanbo.core.constants.PushActionEnum;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.constants.TradeCode;
import com.zhuanbo.core.constants.UserEnum;
import com.zhuanbo.core.constants.UserIncomeOperateType;
import com.zhuanbo.core.constants.UserLevelEnum;
import com.zhuanbo.core.constants.UserRealedEnum;
import com.zhuanbo.core.dto.AdminCheckUserDTO;
import com.zhuanbo.core.dto.AdminModifyUser;
import com.zhuanbo.core.dto.AdminPointDTO;
import com.zhuanbo.core.dto.AdminRealNameDTO;
import com.zhuanbo.core.dto.AdminUserDTO;
import com.zhuanbo.core.dto.InvestorsPriceDTO;
import com.zhuanbo.core.dto.MobileStaUserTeamDTO;
import com.zhuanbo.core.dto.MqUserLevelDTO;
import com.zhuanbo.core.dto.RegisterDTO;
import com.zhuanbo.core.dto.SessionKeyOpenIdDTO;
import com.zhuanbo.core.dto.WxUserDTO;
import com.zhuanbo.core.entity.LevelChangeRecode;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBindThird;
import com.zhuanbo.core.entity.UserIncome;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.AESCoder;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.ContentUtil;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.MliveClientUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.core.vo.UserUpgradePointVO;
import com.zhuanbo.external.service.wx.service.IWeixinIndependenceService;
import com.zhuanbo.service.mapper.UserMapper;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.ILevelChangeRecodeService;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserBindThirdService;
import com.zhuanbo.service.service.IUserBuyInviteCodeService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserPartnerService;
import com.zhuanbo.service.service.IUserSecurityCenterService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service.IYinLiUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.CodeParamsVO;
import com.zhuanbo.service.vo.LoginRegisterResultVO;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import com.zhuanbo.service.vo.RealNameVO;
import com.zhuanbo.service.vo.StatUserTeamVO;
import com.zhuanbo.service.vo.UserLoginVO;
import com.zhuanbo.service.vo.UserVO;
import com.zhuanbo.service.vo.WxTokenVO;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
@RefreshScope
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    public static final String ZHUANBO_SHOP_C_INVITECODE_1 = "zhuanbo:shop:c:invitecode:1";
    public static final String BIND_SOURCE_MP = "mp";
    private Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public final static Integer CODE_CACHE_TIME = 300;// 缓存5分钟
    final String SMS_MODE_DEV = "dev";
    public static final String MP_ZBMALL_OPENID = "mp:zbmall:openId";
    private final static String LOCK_USER_UPDATE = "lock_user_update_";
    public final static String MY_TEAM = "我的团队";

    @Value("${sms-mode}")
    private String smsMode;

    @Autowired
    private IUserBindThirdService iShopUserBindThirdService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    @Autowired
    private ICashService iCashService;
    @Autowired
	private IDictionaryService dictionaryService;
    @Autowired
    private IUserPartnerService iUserPartnerService;
    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private QueueConfig queueConfig;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IUserSecurityCenterService iUserSecurityCenterService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
    private CommonClient commonClient;
    @Autowired
    private IWeixinIndependenceService iWeixinIndependenceService;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private IUserBuyInviteCodeService iUserBuyInviteCodeService;
    @Autowired
    private IYinLiUserService iYinLiUserService;
    @Autowired
    private ILevelChangeRecodeService iLevelChangeRecodeService;
    @Resource
    private MliveClientUtil mliveClientUtil;

    @Override
    public String sendMobileCode(String template, String mobileCode, String mobile, String platForm) {

        if (SMS_MODE_DEV.equals(smsMode)) {
            int retryTimes = 2;
            boolean isSuf = false;
            do {
                isSuf = RedisUtil.set(mobile, "0", CODE_CACHE_TIME);
            } while (!isSuf && retryTimes-- < 0);
            RedisUtil.hset(ConstantsEnum.REDIS_KEY_AREA_CODE.value().toString(), mobile, mobileCode);
            return "OK";
        } else {
            String code = CharUtil.getRandomNum(4);
            log.info("验证码：{},使用模板：{}", code, template);
            int retryTimes = 2;
            boolean isSuf = false;
            do {
                isSuf = RedisUtil.set(mobile, code, CODE_CACHE_TIME);
            } while (!isSuf && retryTimes-- < 0);

            if (isSuf) {
                RedisUtil.hset(ConstantsEnum.REDIS_KEY_AREA_CODE.value().toString(), mobile, mobileCode);
                // 发送验证码 start,调用第三方接口
                SmsDTO dto = SmsDTO.builder().mobile(mobile).mobileCode(mobileCode).platForm(platForm).templateId(template).json("{\"code\":\"" + code + "\"}").build();
                ResponseDTO responseDTO = commonClient.sendSms(dto);
                if(Constants.SUCCESS_CODE.equalsIgnoreCase(responseDTO.getCode())){
                    return "OK";
                }
            }

            return "BAD";
        }
    }

    @Override
    public UserLoginVO packageUser(User user, boolean makeUserToken) {
//        UserPartner userPartner = iUserPartnerService.getById(user.getId());

        UserLoginVO userLoginVo = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVo);
//        userLoginVo.setAuthNo(userPartner.getAuthNo());
//        userLoginVo.setAuthDate(userPartner.getAuthDate());
//        userLoginVo.setTeamName(userPartner.getTeamName());
        if (makeUserToken) {
            userLoginVo.setUserToken(CharUtil.getRandomString(32));
            RedisUtil.set(userLoginVo.getUserToken(), user.getId(), 3600 * 24 * 30);
        }
        
        String cardNoAbbr = null;
        if (!StringUtils.isBlank(user.getCardNo())) {
			String aesKey = dictionaryService.findForString("SecretKey", "AES");
    		String aesIv = dictionaryService.findForString("SecretKey", "IV");

    		try {
    			cardNoAbbr = AESCoder.decrypt(user.getCardNo(), aesKey, aesIv);
    			cardNoAbbr = cardNoAbbr.substring(0, 3) + "******" + cardNoAbbr.substring(cardNoAbbr.length() - 4, cardNoAbbr.length());
		        userLoginVo.setCardNoAbbr(cardNoAbbr);
    		} catch (Exception e) {
    			log.error("userId={},cardNo={}, 解密失败", user.getId(), user.getCardNo());
    			throw new ShopException(31102);
    		}
        }
        
        // openId and token
        List<UserBindThird> userBindThirdList = iShopUserBindThirdService.list(new QueryWrapper<UserBindThird>().eq("user_id", user.getId()));
        if (!userBindThirdList.isEmpty()) {
            for (UserBindThird ub : userBindThirdList) {
                if (ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.stringValue().equals(ub.getBindType())) {
                    userLoginVo.setOpenId(ub.getOpenId());
                    userLoginVo.setWxName(ub.getNickname());
                    // break;
                } else if (ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue().equals(ub.getBindType())) {
                    userLoginVo.setWxOpenId(ub.getOpenId());
                }
            }
        }
        if (StringUtils.isNotBlank(user.getPassword())) {
            userLoginVo.setHasPwd(1);
        }
        /*Object o = RedisUtil.get(MP_ZBMALL_OPENID);// 小程序openId
        if (o != null) {
            userLoginVo.setWxOpenId(String.valueOf(o));
        }*/
        userLoginVo.setBuyInviteCodeList(iUserBuyInviteCodeService.makeBuyInviteCodeList(user));

        return userLoginVo;
    }

    @Override
    public IPage pageManual(IPage<UserVO> page, Map<String, Object> ew) {
        page.setRecords(baseMapper.pageManual(page, ew));
        return page;
    }

    @Override
    public void upgradeLevel(Long userId) {

    }

    @Override
    public List<Object> listUserIdAndLevel(Map<String, Object> params) {
        return baseMapper.listUserIdAndLevel(params);
    }

    @Override
    public Object getUserIdAndLevel(Long userId) {
        return baseMapper.getUserIdAndLevel(userId);
    }

    @Override
    public void removeUserCache(Serializable id) {
        RedisUtil.del(ConstantsEnum.REDIS_USER_MPMALL.stringValue() + id);
    }

    @Override
    public void removeUserCacheList(Collection<Long> ids) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(ids)) {
            return;
        }
        ids.forEach(x -> removeUserCache(x));
    }

    @Override
    public String generateInviteCode() {
        Object o = RedisUtil.sGetOne(ConstantsEnum.REDIS_INVITE_CODE_SET.stringValue());
        return o.toString();
    }

    @Override
    public void checkInviteCodeNumber() {

        boolean tryLock = redissonLocker.tryLock(ConstantsEnum.REDIS_INVITE_CODE_LOCK.stringValue(), TimeUnit.SECONDS, 1, 60);
        if (tryLock) {
            try {
                long incr = 100000L;// 每次增加10万个
                long leftover = RedisUtil.sGetSetSize(ConstantsEnum.REDIS_INVITE_CODE_SET.stringValue());
                if (leftover == 0L) {// 空的
                    Object o = RedisUtil.get(ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue());
                    if (o == null) {
                        makeInviteCode2Set(incr, incr);
                    } else {
                        makeInviteCode2Set(Long.valueOf(o.toString()), incr);
                    }
                } else {
                    // 少于30%重新生成一批
                    long limit = new Double(incr * 0.3).longValue();// 剩下30%
                    if (limit < leftover) {
                        return;
                    }
                    makeInviteCode2Set(Long.valueOf(RedisUtil.get(ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue()).toString()), incr);
                }
            } finally {
                redissonLocker.unlock(ConstantsEnum.REDIS_INVITE_CODE_LOCK.stringValue());
            }
        }
    }

    @Override
    public User findByInviteCode(String inviteCode) {
        if (StringUtils.isBlank(inviteCode)) {
            return null;
        }
        User user = null;
        if (inviteCode.matches(ConstantsEnum.MOBILE_REG.stringValue())) {
            user = getOne(new QueryWrapper<User>().eq("mobile", inviteCode).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));
        }
        return user != null ? user : getOne(new QueryWrapper<User>().eq("invite_code", inviteCode).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));
    }

    @Override
    public RealNameVO realName(Map<String, Object> map) {
        map.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_USER_REAL_NAME.String());
        JSONObject send = iCashService.send(map);
        if (send == null) {
            throw new ShopException("请求失败");
        }
        if (!ReqResEnum.C_10000.String().equals(send.getString(ReqResEnum.CODE.String()))) {
            throw new ShopException(send.getString(ReqResEnum.MSG.String()));
        }

        RealNameVO realNameVO = JSON.parseObject(send.toJSONString(), RealNameVO.class);
        Long userId = (Long) map.get("userId");

        User user = getById(userId);
        user.setRealed(realNameVO.getRealed());
        user.setCardType(realNameVO.getCardType());
        user.setCardNo(realNameVO.getCardNo());
        updateById(user);
        return realNameVO;
    }

    @Override
    public Object realNameList(AdminRealNameDTO dto)throws Exception {
        String aesKey = dictionaryService.findForString("SecretKey", "AES");
        String aesIv = dictionaryService.findForString("SecretKey", "IV");
        Integer page = dto.getPage()<=0?1:dto.getPage();
        Integer limit = dto.getLimit()<=0?10:dto.getLimit();
        Map<String, Object> params = new HashMap<>();
        if(dto.getUserId()!=null){
            params.put("userId",dto.getUserId());
        }
        if(StringUtils.isNotBlank(dto.getOrderNo())){
            params.put("orderNo",dto.getOrderNo());
        }
        if(StringUtils.isNotBlank(dto.getCardNo())){
            params.put("cardNo",AESCoder.encrypt(dto.getCardNo(), aesKey, aesIv));
        }
        Integer total = this.baseMapper.countBuyerList(params);
        // 开始索引
        int fromIndex = (page-1) * limit;
        params.put("start",fromIndex);
        params.put("page",limit);
        List<AdminRealNameDTO> adminRealNameDTOS = this.baseMapper.queryBuyerList(params);
        Map<String, Object> map = new HashMap<>();
        map.put("total",total);
        map.put("item",adminRealNameDTOS);
        return ResponseUtil.ok(map);
    }

    /*@Override
    public User getById(Serializable id) {
        Object o = RedisUtil.get(ConstantsEnum.REDIS_USER_ZBMALL.stringValue() + id);
        if (o == null) {
            User user = super.getById(id);
            if (user != null) {
                UserRedisVO userRedisVO = new UserRedisVO();// 原来的User的password会被过滤掉
                BeanUtils.copyProperties(user, userRedisVO);
                RedisUtil.set(ConstantsEnum.REDIS_USER_ZBMALL.stringValue() + id, userRedisVO, 300);
            }
            return user;
        }
        if (o instanceof User) {
            return (User) o;
        }
        UserRedisVO userRedisVO = (UserRedisVO) o;
        User user = new User();
        BeanUtils.copyProperties(userRedisVO, user);
        return user;
    }*/

    @Override
    public boolean updateById(User entity) {
        Optional.ofNullable(entity).ifPresent(x -> removeUserCache(x.getId()));
        // 同步用户数据到直播系统
        iRabbitMQSenderService.send(RabbitMQSenderImpl.LIVE_USER_UPDATE, entity);
        
        return super.updateById(entity);
    }

    @Override
    public boolean update(User entity, Wrapper<User> updateWrapper) {
        Optional.ofNullable(entity).ifPresent(x -> removeUserCache(x.getId()));
        return super.update(entity, updateWrapper);
    }

    @Override
    public boolean updateBatchById(Collection<User> entityList) {
        if (entityList != null && entityList.size() > 0) {
            for (User user : entityList) {
                Optional.ofNullable(user).ifPresent(x -> removeUserCache(x.getId()));
            }
        }
        return super.updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdate(User entity) {
        Optional.ofNullable(entity).ifPresent(x -> removeUserCache(x.getId()));
        return super.saveOrUpdate(entity);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<User> entityList) {
        if (entityList != null && entityList.size() > 0) {
            for (User user : entityList) {
                Optional.ofNullable(user).ifPresent(x -> removeUserCache(x.getId()));
            }
        }
        return super.saveOrUpdateBatch(entityList);
    }

    @Override
    public Map<String, Object> team(MobileStaUserTeamDTO statUserTeamDTO) throws Exception {

        List<Object> childrenList = new ArrayList<Object>();
        iUserInviteService.getTeam(childrenList, statUserTeamDTO.getUserId());
        //获取用户所有子集
        List<Long> userIds = new ArrayList<Long>();
        Long childrenId;
        Integer childrenPtLevel;
        for (Object obj : childrenList) {
            childrenId = Long.parseLong(obj.toString().split(",")[0]);
            childrenPtLevel = Integer.parseInt(obj.toString().split(",")[1]);
            if (statUserTeamDTO.getPtLevel() == childrenPtLevel) {
                userIds.add(childrenId);
            }
        }
        Page<User> page = new Page<>(statUserTeamDTO.getPage(), statUserTeamDTO.getLimit());
        IPage<User> iPage = null;
        List<StatUserTeamVO> voList = new ArrayList<StatUserTeamVO>();
        if (null != userIds && userIds.size() > 0) {
            QueryWrapper<User> qw = new QueryWrapper<User>()
                    .select("id", "head_img_url", "nickname", "pt_level", "pt_no", "reg_date")
                    .eq("status", ConstantsEnum.USER_STATUS_1.integerValue())
                    .in("id", userIds)
                    .orderByDesc("id");

            iPage = this.page(page, qw);
            if (null != iPage && !CollectionUtils.isEmpty(iPage.getRecords())) {
                //获取直属子集
                List<Object> underList = iUserInviteService.getChildren(statUserTeamDTO.getUserId());
                for (User obj : iPage.getRecords()) {
                    String under = null;
                    if (null != underList) {
                        //循环用户是否直属
                        for (Object ul : underList) {
                            childrenId = Long.parseLong(ul.toString().split(",")[0]);
                            if (obj.getId().longValue() == childrenId.longValue()) {
                                under = "直属";
                                break;
                            }
                        }
                    }
                    StatUserTeamVO vo = new StatUserTeamVO();
                    BeanUtils.copyProperties(obj, vo);
                    vo.setUserId(statUserTeamDTO.getId());
                    vo.setUnder(under);
                    voList.add(vo);
                }
            }
        }
        if (iPage == null) {
            iPage = page;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());
        data.put("items", voList);
        return data;
    }

    /**
     * @param statUserTeamDTO
     * @return :java.lang.Object
     * @Description(描述): 合团队人数统计(包含团队)
     * @auther: Jack Lin
     * @date: 2019/8/17 11:54
     */
    @Override
    public Map<String, Object> teamCount(MobileStaUserTeamDTO statUserTeamDTO) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public Map<String, Object> updateUser(AdminUserDTO user) throws Exception {

        String key = LOCK_USER_UPDATE + user.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 120);
        if (!b) {
            throw new ShopException(30014);
        }
        Map<String, Object> backMap = new HashMap<>();
        try {

            if (user.getOperateType().intValue() == 0) {

                update(new User(), new UpdateWrapper<User>().eq("id", user.getId())
                        .set("shield", 1)
                        .set("update_time", LocalDateTime.now())
                        .set("operator", iAdminService.getAdminName(user.getOperatorId())));
            } else if (user.getOperateType().intValue() == 1) {

                update(new User(), new UpdateWrapper<User>().eq("id", user.getId())
                        .set("shield", 0)
                        .set("update_time", LocalDateTime.now())
                        .set("operator", iAdminService.getAdminName(user.getOperatorId())));
            } else if (user.getOperateType().intValue() == 2) {

                update(new User(), new UpdateWrapper<User>().eq("id", user.getId())
                        .set("status", 2)
                        .set("update_time", LocalDateTime.now())
                        .set("operator", iAdminService.getAdminName(user.getOperatorId())));
            } else if (user.getOperateType().intValue() == 3) {

                update(new User(), new UpdateWrapper<User>().eq("id", user.getId())
                        .set("status", 1)
                        .set("update_time", LocalDateTime.now())
                        .set("operator", iAdminService.getAdminName(user.getOperatorId())));
            } else if (4 == user.getOperateType().intValue()) {
/**

                User old = this.getById(user.getId());
                Integer oldPtLevel = old.getPtLevel();

                Map<String, Object> changeUserLevelMap = iUserLevelService.changeUserLevel(user.getId(), user.getPtLevel());
                if (changeUserLevelMap != null) {
                    backMap.putAll(changeUserLevelMap);
                }
                // 生成邀请码之类的
                if (UserEnum.PT_LEVEL_0.Integer().equals(oldPtLevel) && user.getPtLevel() > oldPtLevel) {
                    start2High2Gift(old, user.getPtLevel());
                }
                // 授权日期
                if (oldPtLevel < UserEnum.PT_LEVEL_4.Integer() && user.getPtLevel() > UserEnum.PT_LEVEL_3.Integer()) {
                    iUserPartnerService.generateAutoNo(old);
                }

                this.update(new User(), new UpdateWrapper<User>()
                        .set("operator", iAdminService.getAdminName(user.getOperatorId()))
                        .set("pt_level", user.getPtLevel())
                        .set("update_time", LocalDateTime.now())
                        .eq("id", user.getId()));
                //
                iGraphService.txUpdateUserNOf(user.getId(), user.getPtLevel(), null, null);
                // 合作人以上，快速升级
                if (user.getPtLevel() > UserEnum.PT_LEVEL_3.Integer()) {
                    UserInvite userInvite = iUserInviteService.getById(user.getId());
                    rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getLevelToLevel().getRoutingKey(),
                            "{\"uid\":"+ userInvite.getPid() + "}");
                } * 
 */
            } else if (5 == user.getOperateType().intValue()) {//修改密码
                String password = user.getPassword();
                update(new User(), new UpdateWrapper<User>().eq("id", user.getId())
                        .set("password", DigestUtils.sha1Hex(password.getBytes("UTF-16LE")))
                        .set("update_time", LocalDateTime.now())
                        .set("operator", iAdminService.getAdminName(user.getOperatorId())));
            }
            // 同步分润系统
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
            removeUserCache(user.getId());
        } catch (Exception e) {
            log.error("用户更变失败:{}", e);
            throw new RuntimeException("用户更变失败");
        } finally {
            if (b) {
                redissonLocker.unlock(key);
            }
        }
        return backMap;
    }

    @Override
    public void start2High2Gift(User user, Integer level) {
        if (user.getPtLevel() >= level || !UserEnum.PT_LEVEL_0.Integer().equals(user.getPtLevel())) {
            return;
        }
        iUserIncomeService.makeUserIncome(user.getId());

        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        if (StringUtils.isBlank(user.getInviteCode()) || StringUtils.isBlank(user.getPtNo())) {
            if (StringUtils.isBlank(user.getInviteCode())) {
                String inviteCode = generateInviteCode();
                userUpdateWrapper.set("invite_code", inviteCode);
                user.setInviteCode(inviteCode);
            }
            if (StringUtils.isBlank(user.getPtNo())) {
                String ptNo = iSeqIncrService.nextVal("pt_no", 6, Align.LEFT);
                userUpdateWrapper.set("pt_no", ptNo);
                user.setPtNo(ptNo);
            }
            userUpdateWrapper.eq("id", user.getId());
            update(new User(), userUpdateWrapper);
            removeUserCache(user.getId());
        }
    }

    /**
     * 用户提现绑定银行卡列表
     *
     * @param dto AdminUserDTO
     * @return
     */
    @Override
    public Object withdrBankList(AdminUserDTO dto) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        map.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        map.put("requestId",System.currentTimeMillis());
        map.put("sysCnl","WEB");
        map.put("timestamp", DateUtil.getSecondTimestamp(System.currentTimeMillis()));
        map.put("clientIp", MDC.get("CLIENT_IP"));
        map.put("userId", dto.getId());
        map.put("mobile", dto.getMobile());
        map.put("page", dto.getPage());
        map.put("limit", dto.getLimit());
        String plain = Sign.getPlain(map);
        plain += "&key=" + authConfig.getMercPrivateKey();
        String sign = Sign.sign(plain);
        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
        String payIp = authConfig.getPayUrlIp();
        String url = payIp + PayInterfaceEnum.WITHER_BANK_LIST.getId();

        log.info("|提现银行卡列表|调用MPPAY接口|请求 url:{}, header: {},参数:{}|", url, JacksonUtil.objTojson(headers), plain);
        String respMsg = HttpUtil.sendPostJson(url, map, headers);
        log.info("|提现银行卡列表|调用MPPAY接口|结果：{}|", respMsg);

        Map<String, Object> respMap;
        if (StringUtils.isNotBlank(respMsg)) {
            respMap = JacksonUtil.jsonToMap(respMsg);
            String code = (String) respMap.get("code");
            if (Constants.SUCCESS_CODE.equalsIgnoreCase(code)) {
                Map<String,Object> dataMap = (Map<String,Object>) respMap.get(Constants.DATA);
                List<Map<String,Object>> itemList = new ArrayList<>();
                if(dataMap != null){
                    itemList = (ArrayList<Map<String,Object>>) dataMap.get("items");
                    if(itemList != null && itemList.size() > 0){
                        List<Long> userIds = itemList.stream().map(itemMap -> Long.valueOf((Integer)itemMap.get("userId"))).collect(Collectors.toList());
                        List<User> users = (List<User>) listByIds(userIds);
                        Map<Long, List<User>> usersMap = users.stream().collect(Collectors.groupingBy(User::getId));
                        itemList.forEach(data -> {
                            Long userId = Long.valueOf((Integer)data.get("userId"));
                            List<User> userList = usersMap.get(userId);
                            if (userList != null && userList.size() > 0) {
                                User user = userList.get(0);
                                data.put("name", user.getName());
                                data.put("nickname", user.getNickname());
                                data.put("status", user.getRealed());
                                data.put("operator", user.getOperator());
                            }
                        });
                    }
                    dataMap.put("items",itemList);
                }
                return ResponseUtil.ok(dataMap);
            }
            return ResponseUtil.fail(10011, (String) respMap.get("msg"));
        }
        return ResponseUtil.fail(10011);
    }

    /**
     * 用户快捷绑定银行卡列表
     *
     * @param dto AdminUserDTO
     * @return
     */
    @Override
    public Object quickBankList(AdminUserDTO dto) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        map.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        map.put("requestId",System.currentTimeMillis());
        map.put("sysCnl","WEB");
        map.put("timestamp", DateUtil.getSecondTimestamp(System.currentTimeMillis()));
        map.put("clientIp", MDC.get("CLIENT_IP"));
        map.put("userId", dto.getId());
        map.put("mobile", dto.getMobile());
        map.put("page", dto.getPage());
        map.put("limit", dto.getLimit());
        String plain = Sign.getPlain(map);
        plain += "&key=" + authConfig.getMercPrivateKey();
        String sign = Sign.sign(plain);
        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
        String payIp = authConfig.getPayUrlIp();
        String url = payIp + PayInterfaceEnum.QUICK_BANK_LIST.getId();

        log.info("|快捷支付银行卡列表|调用MPPAY接口|请求 url:{}, header: {},参数:{}|", url, JacksonUtil.objTojson(headers), plain);
        String respMsg = HttpUtil.sendPostJson(url, map, headers);
        log.info("|快捷支付银行卡列表|调用MPPAY接口|结果：{}|", respMsg);

        Map<String, Object> respMap;
        if (StringUtils.isNotBlank(respMsg)) {
            respMap = JacksonUtil.jsonToMap(respMsg);
            String code = (String) respMap.get("code");
            if (Constants.SUCCESS_CODE.equalsIgnoreCase(code)) {
                Map<String,Object> dataMap = (Map<String,Object>) respMap.get(Constants.DATA);
                List<Map<String,Object>> itemList = new ArrayList<>();
                if(dataMap != null){
                    itemList = (ArrayList<Map<String,Object>>) dataMap.get("items");
                    if(itemList != null && itemList.size() > 0){
                        List<Long> userIds = itemList.stream().map(itemMap -> Long.valueOf((Integer)itemMap.get("userId"))).collect(Collectors.toList());
                        List<User> users = (List<User>) listByIds(userIds);
                        Map<Long, List<User>> usersMap = users.stream().collect(Collectors.groupingBy(User::getId));
                        itemList.forEach(data -> {
                            Long userId = Long.valueOf((Integer)data.get("userId"));
                            List<User> userList = usersMap.get(userId);
                            if (userList != null && userList.size() > 0) {
                                User user = userList.get(0);
                                data.put("nickname", user.getNickname());
                                data.put("name", user.getName());
                            }
                        });
                    }
                    dataMap.put("items",itemList);
                }
                return ResponseUtil.ok(dataMap);
            }
            return ResponseUtil.fail(10011, (String) respMap.get("msg"));
        }
        return ResponseUtil.fail(10011);
    }

    @Override
    public Object cancelRealname(Integer adminId, Long userId) throws Exception {
        // [mppay].[user_oper] 更改为未实名
        Map<String, Object> map = new HashMap<>();
        map.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        map.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        map.put("requestId",System.currentTimeMillis());
        map.put("sysCnl","WEB");
        map.put("timestamp", DateUtil.getSecondTimestamp(System.currentTimeMillis()));
        map.put("clientIp", MDC.get("CLIENT_IP"));
        map.put("userId", userId);
        String plain = Sign.getPlain(map);
        plain += "&key=" + authConfig.getMercPrivateKey();
        String sign = Sign.sign(plain);
        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
        String payIp = authConfig.getPayUrlIp();
        String url = payIp + PayInterfaceEnum.CANCEL_REALNAME.getId();
        log.info("|实名信息置为无效|调用MPPAY接口|请求 url:{}, header: {},参数:{}|", url, JacksonUtil.objTojson(headers), plain);
        String respMsg = HttpUtil.sendPostJson(url, map, headers);
        log.info("|实名信息置为无效|调用MPPAY接口|结果：{}|", respMsg);

        Map<String, Object> respMap;
        if (StringUtils.isBlank(respMsg)) {
            return ResponseUtil.fail(10012);
        }
        respMap = JacksonUtil.jsonToMap(respMsg);
        String code = (String) respMap.get("code");
        if (!Constants.SUCCESS_CODE.equalsIgnoreCase(code)) {
            return ResponseUtil.fail(code, (String) respMap.get("msg"));
        }

        // [shop_user]:实名信息更改为:未实名
        User user = getById(userId);
        if(user == null){
            log.error("|用户实名置为无效|用户不存在");
            return ResponseUtil.badResult();
        }
        String adminName = iAdminService.getAdminName(adminId);
        user.setRealed(UserRealedEnum.UNREAL.getId());
        user.setCardType(0);
        user.setOperator(adminName);
        updateById(user);
        return ResponseUtil.ok();
    }

    @Transactional
    @Override
    public LoginRegisterResultVO commonRegister(RegisterDTO registerDTO) {

        // 邀请码校验
        Optional.ofNullable(StringUtils.stripToNull(registerDTO.getInviteCode()))
                .orElseThrow(() -> new ShopException(10047));
        User inviter = findByInviteCode(registerDTO.getInviteCode());
        Optional.ofNullable(inviter).orElseThrow(() -> new ShopException(10047));
        // mobile判断
        checkMobileExist(registerDTO);
        // 验证码判断
        checkMobileCode(registerDTO.getMobile(), registerDTO.getCode(), registerDTO);
        //
        User user = new User();
        // 注册基本信息
        doUser(user, registerDTO);
        // 关系表
        iUserInviteService.doUserInvite(user, inviter.getId());
        // 副表
        iUserPartnerService.simpleGenerate(user);
        // 收益表
        iUserIncomeService.makeUserIncome(user.getId());
        // 用户安全中心
        iUserSecurityCenterService.doUserSecurityCenter(user);
        // 硬同步到live
        synchronize2Live(user);

        LoginRegisterResultVO loginRegisterResultVO = new LoginRegisterResultVO();
        loginRegisterResultVO.setUser(user);
        loginRegisterResultVO.setInviter(inviter);
        return loginRegisterResultVO;
    }

    private void checkMobileExist(RegisterDTO registerDTO) {
        if (count(new QueryWrapper<User>().eq("mobile", registerDTO.getMobile()).eq("deleted", ConstantsEnum.DELETED_0.integerValue())) != 0) {
            throw new ShopException(10001);
        }
    }

    @Transactional
    @Override
    public User mpCommonRegister(RegisterDTO registerDTO, Long pid) {

        // 注册
        LoginRegisterResultVO loginRegisterResultVO = registerEntrance(registerDTO, true);
        // 绑定关系
        Object o = RedisUtil.get(MapKeyEnum.ZBMALL_OPENID_BEAN.value() + registerDTO.getMobile());
        if (o != null) {
            SessionKeyOpenIdDTO sessionKeyOpenIdDTO = (SessionKeyOpenIdDTO) o;
            LocalDateTime now = LocalDateTime.now();
            UserBindThird userBindThird = new UserBindThird();
            userBindThird.setUserId(loginRegisterResultVO.getUser().getId());
            userBindThird.setBindId(sessionKeyOpenIdDTO.getUnionid());
            userBindThird.setOpenId(sessionKeyOpenIdDTO.getOpenid());
            userBindThird.setAddTime(now);
            userBindThird.setUpdateTime(now);
            userBindThird.setBindType(ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue());
            userBindThird.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
            userBindThird.setBindStatus(1);
            userBindThird.setRegDate(DateUtil.toyyyy_MM_dd(now));
            userBindThird.setRegTime(DateUtil.toHH_mm_ss(now));
            iShopUserBindThirdService.save(userBindThird);
            RedisUtil.del(MapKeyEnum.ZBMALL_OPENID_BEAN.value() + registerDTO.getMobile());
        }
        return loginRegisterResultVO.getUser();
    }

    @Override
    public void checkMobileCode(String mobile, String code, RegisterDTO registerDTO) {

        if (registerDTO != null && registerDTO.getWithoutCode().equals(1)) {

            if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
                throw new ShopException("手机号或验证码不能为空");
            }
            if (!RedisUtil.hasKey(mobile)) {
                throw new ShopException(10017);
            }
            if (!RedisUtil.get(mobile).toString().equals(code)) {
                throw new ShopException(10019);
            }
        }
    }

    @Override
    public Long getInvitePid(CodeParamsVO codeParamsVO) {
        User inviteUser = findByInviteCode(codeParamsVO.getInviteCode());
        if (inviteUser == null) {
            throw new ShopException(10047);
        }
        return inviteUser.getId();
        /*if(StringUtils.isNotBlank(codeParamsVO.getInviteCode())) {
            User inviteUser = findByInviteCode(codeParamsVO.getInviteCode());
            if (inviteUser == null || ConstantsEnum.USER_PT_LEVEL_0.integerValue().equals(inviteUser.getPtLevel())) {
                throw new ShopException(10047);
            }
            if (ConstantsEnum.USER_PT_LEVEL_0.integerValue().equals(inviteUser.getPtLevel())) {// M星人分享的，则上级账号是公司
                return iDictionaryService.findForLong(ConstantsEnum.MALL_USER.stringValue(), ConstantsEnum.COMPANY_USER_ID.stringValue());
            } else {
                return inviteUser.getId();
            }
        } else {
            return iDictionaryService.findForLong(ConstantsEnum.MALL_USER.stringValue(), ConstantsEnum.COMPANY_USER_ID.stringValue());
        }*/
    }

    @Override
    public boolean isNotRegisterMobileThrowEx(String mobile) {
        if (count(new QueryWrapper<User>().eq("mobile", mobile).eq("deleted", ConstantsEnum.DELETED_0.integerValue())) != 0) {
            throw new ShopException(10001);
        }
        return true;
    }

    @Override
    public Object testcc(Long id) {
        return baseMapper.testcc(id);
    }

    @Transactional
    @Override
    public Map<String, Object> updateUserByMQ(MqUserLevelDTO mqUserLevelDTO) {

        HashMap<String, Object> stringObjectHashMap = new HashMap<>();

        User user = getById(mqUserLevelDTO.getUserId());
        boolean update = update(new User(), new UpdateWrapper<User>()
                .set("status", mqUserLevelDTO.getStatus())
                .set("pt_level", mqUserLevelDTO.getLevel())
                .eq("id", mqUserLevelDTO.getUserId()));
        if (!update) {
            log.error("MQ消息更新用户信息失败:{}", mqUserLevelDTO);
            throw new ShopException("MQ消息更新用户信息失败");
        }
        iUserInviteService.update(new UserInvite(), new UpdateWrapper<UserInvite>()
                .set("pid", mqUserLevelDTO.getInviteUpUserId()).eq("id", mqUserLevelDTO.getUserId()));
        // 通知
        if (user.getPtLevel() < mqUserLevelDTO.getLevel()) {

            List<NotifyPushMQVO> notifyPushMQVOList = new ArrayList<>();

            NotifyPushMQVO notifyPushMQVO = new NotifyPushMQVO();
            notifyPushMQVO.setTitle(CopyWritingConstants.PUSH_TITLE);
            String replaceContent = CopyWritingConstants.USER_UPGRADE_v2.replace("$", UserLevelEnum.desc(mqUserLevelDTO.getLevel()))
                    .replace("w", CopyWritingConstants.USER_UPGRADE_v2_WHERE);
            notifyPushMQVO.setContent(replaceContent);
            notifyPushMQVO.setExtra(MapUtil.of("type", 3, "link", ""));
            notifyPushMQVO.setMsgFlag(1);
            notifyPushMQVO.setUserId(user.getId());
            notifyPushMQVO.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
            notifyPushMQVO.setNickname(user.getNickname());
            notifyPushMQVO.setAction(PushActionEnum.UPGRADE.value());
            notifyPushMQVOList.add(notifyPushMQVO);

            UserInvite userInvite = iUserInviteService.getById(user.getId());
            if (userInvite != null) {
                User p = getById(userInvite.getPid());
                if (p != null) {
                    notifyPushMQVO = new NotifyPushMQVO();
                    notifyPushMQVO.setTitle(CopyWritingConstants.PUSH_TITLE);
                    String content = CopyWritingConstants.USER_UPGRADE_PARENT.replace("$0", user.getNickname())
                            .replace("$1", UserLevelEnum.desc(mqUserLevelDTO.getLevel())).replace("w", CopyWritingConstants.USER_UPGRADE_v2_WHERE);
                    notifyPushMQVO.setContent(content);
                    notifyPushMQVO.setExtra(MapUtil.of("type", 3, "link", ""));
                    notifyPushMQVO.setMsgFlag(1);
                    notifyPushMQVO.setUserId(p.getId());
                    notifyPushMQVO.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
                    notifyPushMQVO.setNickname(p.getNickname());
                    notifyPushMQVO.setAction(PushActionEnum.UPGRADE.value());
                    notifyPushMQVOList.add(notifyPushMQVO);
                }
            }
            stringObjectHashMap.put(MapKeyEnum.PUSH_NOTIFY_LIST.value(), notifyPushMQVOList);
        }
        RedisUtil.del(String.valueOf(mqUserLevelDTO.getUserId()));

        return stringObjectHashMap;
    }

    /**
     * 保存基本用户信息
     * @param user
     * @param registerDTO
     */
    private void doUser(User user, RegisterDTO registerDTO) {

        LocalDateTime now = LocalDateTime.now();

        user.setUserName(registerDTO.getMobile());
        user.setNickname(registerDTO.getMobile());
        user.setName(registerDTO.getMobile());
        user.setMobile(registerDTO.getMobile());
        user.setGender(Integer.parseInt(ConstantsEnum.USER_GENDER_0.value().toString()));
        user.setStatus(Integer.parseInt(ConstantsEnum.USER_STATUS_1.value().toString()));
        user.setRegDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        user.setRegTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        user.setAddTime(now);
        user.setUpdateTime(now);
        user.setInviteCode(generateInviteCode());

        Object areaCode = RedisUtil.hget(ConstantsEnum.REDIS_KEY_AREA_CODE.value().toString(), registerDTO.getMobile());
        user.setAreaCode(areaCode == null ? "86" : areaCode.toString());
        user.setPtLevel(ConstantsEnum.USER_PT_LEVEL_0.integerValue());

        save(user);
    }

    @Transactional
    @Override
    public void modifyMobile(Integer adminId, AdminModifyUser adminModifyUser) {

        if (adminModifyUser.getUserId() != null && StringUtils.isNotBlank(adminModifyUser.getNewMobile())) {

            User old = getById(adminModifyUser.getUserId());
            if (!adminModifyUser.getOldMobile().equals(old.getMobile())) {
                throw new ShopException("旧的手机号不正确");
            }
            User one = getOne(new QueryWrapper<User>()
                    .eq("mobile", adminModifyUser.getNewMobile()).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));
            if (one != null) {
                throw new ShopException("新的手机号已存在");
            }

            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.set("user_name", adminModifyUser.getNewMobile());
            userUpdateWrapper.set("mobile", adminModifyUser.getNewMobile());
            userUpdateWrapper.set("operator", iAdminService.getAdminName(adminId));
            userUpdateWrapper.eq("id", adminModifyUser.getUserId());
            update(new User(), userUpdateWrapper);

            //解绑
            iShopUserBindThirdService.remove(new QueryWrapper<UserBindThird>().eq("user_id",adminModifyUser.getUserId()));

            // 清空缓存
            removeUserCache(adminModifyUser.getUserId());
        }
    }

    /**
     * 往redis.set放入数据
     * @param lastIndex 上一次添加的邀请码最后个标记
     * @param incr 每次增加量
     */
    private void makeInviteCode2Set(long lastIndex, long incr) {
        // 过滤公司邀请码
        User u = getById(1L);
        List<Long> inviteCodeList = new ArrayList<>();
        for (long i = lastIndex; i < (lastIndex + incr); i++) {
            if (Long.valueOf(u.getInviteCode()).equals(i)) {
                continue;
            }
            inviteCodeList.add(i);
        }
        Collections.shuffle(inviteCodeList);// 打乱
        Long[] messInviteCodeList = inviteCodeList.stream().toArray(Long[]::new);
        RedisUtil.sSet(ConstantsEnum.REDIS_INVITE_CODE_SET.stringValue(), messInviteCodeList);// 邀请码队列
        RedisUtil.set(ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue(), lastIndex + incr);// 最后生成的邀请码
    }

    @Override
    public UserVO accounInfo(Integer id) {
        return baseMapper.accountInfo(id);
    }

    @Override
    public void synchronize2Live(User user) {
        try {
            //同步user数据到live
            Map<String, Object> request = new HashMap<String, Object>();
            request.put("mercId", authConfig.getMercId());
            request.put("userId", user.getId());
            request.put("nickname", user.getNickname());
            request.put("headImgUrl", user.getHeadImgUrl());
            request.put("mobile", user.getMobile());
            String s = HttpUtil.sendPostJson(authConfig.getLiveUrl() + "/user/addOrUpdate", request, null);
            log.info("请求pay 结果：{}", s);
            if (StringUtils.isBlank(s)) {
                throw new ShopException(10502);
            }

            JSONObject json = JSONObject.parseObject(s);
            if (!ReqResEnum.C_10000.String().equalsIgnoreCase(json.getString(ReqResEnum.CODE.String()))) {
                log.error("请求接口失败,params[{}],response[{}]", JacksonUtil.objTojson(request), json);
                throw new ShopException(json.get("code").toString(), json.get("msg").toString());
            } else {
                json = json.getJSONObject(ReqResEnum.DATA.String());
                Long liveUserId = json.getLongValue("liveUserId");
                user.setLiveUserId(liveUserId);
                updateById(user);
            }
        } catch (Exception e) {
            log.error("同步用户数据live直播失败:{}", e);
            throw new ShopException("同步同步失败");
        }
    }

    @Override
    public UserLoginVO userLoginData(User user, boolean makeUserToken) {
        UserLoginVO userLoginVO = packageUser(user, makeUserToken);
        RedisUtil.del(user.getMobile());// 删除验证码
        removeUserCache(user.getId());
        return userLoginVO;
    }

    @Override
    public List<Map<String, Object>> makeMqDataList(User user, boolean insertDB, MQDataTypeEnum... mqDataTypeEnum) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (MQDataTypeEnum dataTypeEnum : mqDataTypeEnum) {
            switch (dataTypeEnum) {
                case PAY_USER_ADD:
                    mapList.add(makeMqDataListForPayUserAdd(user, insertDB));
                    break;
                case LIVE_USER_ADD:
                    mapList.add(makeMqDataListForLiveUserAdd(user, insertDB));
                    break;
                case USER_MODIFY_PROFIT:
                    mapList.add(makeMqDataListForProfitUserAdd(user, insertDB));
                    break;
                default:
                    log.info("no thing to do for make");
            }
        }
        return mapList;
    }


    @Transactional
    @Override
    public LoginRegisterResultVO registerEntrance(RegisterDTO registerDTO, Boolean backUserLoginData) {

        // 正常注册
        LoginRegisterResultVO loginRegisterResultVO = commonRegister(registerDTO);
        // 推送
        loginRegisterResultVO.setNotifyPushMQVOList(notifyPushMQVOList(loginRegisterResultVO.getUser(), loginRegisterResultVO.getInviter()));
        // mq消息
        loginRegisterResultVO.setMqList(makeMqDataList(loginRegisterResultVO.getUser(), Boolean.TRUE, MQDataTypeEnum.LIVE_USER_ADD, MQDataTypeEnum.PAY_USER_ADD,MQDataTypeEnum.USER_MODIFY_PROFIT));
        if (backUserLoginData) {
            loginRegisterResultVO.setUserLoginVO(userLoginData(loginRegisterResultVO.getUser(), true));
        }
        return loginRegisterResultVO;
    }

    @Override
    public void afterRegister(LoginRegisterResultVO loginRegisterResultVO) {
        if (loginRegisterResultVO == null) {
            return;
        }
        // 推送
        List<NotifyPushMQVO> notifyPushMQVOList = loginRegisterResultVO.getNotifyPushMQVOList();
        if (!CollectionUtils.isEmpty(notifyPushMQVOList)) {
            for (NotifyPushMQVO notifyPushMQVO : notifyPushMQVOList) {
                iRabbitMQSenderService.send(RabbitMQSenderImpl.PUSH_NOTIFY, notifyPushMQVO);
            }
        }
        // mq消息
        List<Map<String, Object>> mapList = loginRegisterResultVO.getMqList();
        if (!CollectionUtils.isEmpty(mapList)) {
            for (Map<String, Object> mm : mapList) {
                iRabbitMQSenderService.send(String.valueOf(mm.get(MapKeyEnum.ACTION.value())),
                        mm.get(MapKeyEnum.DATA.value()));
            }
        }
    }

    @Transactional
    @Override
    public LoginRegisterResultVO loginMPEntrance(RegisterDTO registerDTO) {
        // 正常注册
        LoginRegisterResultVO loginRegisterResultVO = registerEntrance(registerDTO, true);
        // 绑定openid
        Object o = RedisUtil.get(MapKeyEnum.ZBMALL_OPENID_BEAN.value() + registerDTO.getMobile());
        if (o != null) {
            SessionKeyOpenIdDTO sessionKeyOpenIdDTO = (SessionKeyOpenIdDTO) o;
            /*if (StringUtils.isNotBlank(sessionKeyOpenIdDTO.getUnionid())) {
                UserBindThird ub = iShopUserBindThirdService.getOne(new QueryWrapper<UserBindThird>().eq("bind_id", sessionKeyOpenIdDTO.getUnionid()).eq("bind_type", ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue()));
                if (ub != null) {
                    throw new ShopException(10063);
                }
            }*/
            LocalDateTime now = LocalDateTime.now();
            UserBindThird userBindThird = new UserBindThird();
            userBindThird.setUserId(loginRegisterResultVO.getUser().getId());
            userBindThird.setBindId(sessionKeyOpenIdDTO.getUnionid());
            userBindThird.setOpenId(sessionKeyOpenIdDTO.getOpenid());
            userBindThird.setAddTime(now);
            userBindThird.setUpdateTime(now);
            userBindThird.setBindType(ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue());
            userBindThird.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
            userBindThird.setBindStatus(1);
            userBindThird.setRegDate(DateUtil.toyyyy_MM_dd(now));
            userBindThird.setRegTime(DateUtil.toHH_mm_ss(now));
            iShopUserBindThirdService.save(userBindThird);
        }
        return loginRegisterResultVO;
    }

    /**
     * 支付系统mq数据生成
     * @param user
     * @param insertDB
     * @return
     */
    private Map<String, Object> makeMqDataListForPayUserAdd(User user, boolean insertDB){
        String uuid = UUID.randomUUID().toString();
        HashMap<String, Object> mqMap = new HashMap<>();
        HashMap<String, Object> dataHashMap = new HashMap<>();
        dataHashMap.put("mercId", authConfig.getMercId());
        dataHashMap.put("userId", user.getId());
        dataHashMap.put("mobile", user.getMobile());
        dataHashMap.put("email", user.getEmail());
        dataHashMap.put("type", "ADD");
        dataHashMap.put("nickname", user.getNickname());
        dataHashMap.put("uuid", uuid);
        if (insertDB) {
            iMqMessageService.n2Save(0, queueConfig.getExchange(), JSON.toJSONString(dataHashMap),
                    queueConfig.getQueues().getUser().getRoutingKey(), uuid);
        }
        //
        mqMap.put("action", RabbitMQSenderImpl.PAY_ADD_NEW);
        mqMap.put("data", dataHashMap);
        return mqMap;
    }

    private Map<String, Object> makeMqDataListForLiveUserAdd(User user, boolean insertDB){
        String uuid = UUID.randomUUID().toString();
        HashMap<String, Object> mqMap = new HashMap<>();
        HashMap<String, Object> dataHashMap = new HashMap<>();
        dataHashMap.put("mercId", authConfig.getMercId());
        dataHashMap.put("userId", user.getId());
        dataHashMap.put("mobile", user.getMobile());
        dataHashMap.put("email", user.getEmail());
        dataHashMap.put("type", "ADD");
        dataHashMap.put("nickname", user.getNickname());
        dataHashMap.put("headImgUrl", user.getHeadImgUrl());
        dataHashMap.put("uuid", uuid);
        iMqMessageService.n2Save(0, queueConfig.getExchange(), JSON.toJSONString(dataHashMap),
                queueConfig.getQueues().getLiveUser().getRoutingKey(), uuid);
        //
        mqMap.put("action", RabbitMQSenderImpl.LIVE_USER_ADD_NEW);
        mqMap.put("data", dataHashMap);
        return mqMap;
    }

    private Map<String, Object> makeMqDataListForProfitUserAdd(User user, boolean insertDB){
        String uuid = UUID.randomUUID().toString();
        HashMap<String, Object> mqMap = new HashMap<>();
        HashMap<String, Object> dataHashMap = new HashMap<>();
        dataHashMap.put("mercId", authConfig.getMercId());
        dataHashMap.put("userId", user.getId());
        dataHashMap.put("uuid", uuid);
        iMqMessageService.n2Save(0, queueConfig.getExchange(), JSON.toJSONString(dataHashMap),
                queueConfig.getQueues().getUserModifyProfit().getRoutingKey(), uuid);
        //
        mqMap.put("action", RabbitMQSenderImpl.USER_MODIFY_PROFIT_NEW);
        mqMap.put("data", dataHashMap);
        return mqMap;
    }

    /**
     * 推送消息
     * @param user
     * @param inviteUser
     * @return
     */
    private List<NotifyPushMQVO> notifyPushMQVOList(User user, User inviteUser){
        // 推送
        NotifyPushMQVO notifyPushMQVO = new NotifyPushMQVO();
        notifyPushMQVO.setTitle(CopyWritingConstants.PUSH_TITLE);

        notifyPushMQVO.setContent(CopyWritingConstants.USER_INVITE.replace("$", ContentUtil.starName(user.getNickname())));
        notifyPushMQVO.setExtra(MapUtil.of("type", 3, "link", ""));
        notifyPushMQVO.setMsgFlag(1);
        notifyPushMQVO.setUserId(inviteUser.getId());
        notifyPushMQVO.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        notifyPushMQVO.setNickname(inviteUser.getNickname());
        notifyPushMQVO.setAction(PushActionEnum.REGISTER.value());
        return Arrays.asList(notifyPushMQVO);
    }

    /**
     * 手机号和邀请码处理（可能要扩展）
     * @param mobile
     * @param inviteCode
     * @param withoutCode
     */
    @Override
    public void registerCheckMobileNCode(String mobile, String inviteCode, Integer withoutCode) {
        // 手机校验是否存在
        isNotRegisterMobileThrowEx(mobile);
        // withoutCode 0:不要验证码登录，1:要验证码登录
        if (withoutCode.equals(1) && StringUtils.isBlank(inviteCode)) {
            throw new ShopException(10047);
        }
    }

    @Override
    @Transactional
    public LoginRegisterResultVO registerNewWXUser(String appVersion, WxTokenVO wxTokenVO, RegisterDTO registerDTO, String sysCnl) {

        LoginRegisterResultVO loginRegisterResultVO = registerEntrance(registerDTO, true);
        // 注册
        User user = loginRegisterResultVO.getUser();
        LocalDateTime now = LocalDateTime.now();

        UserBindThird userBindThird = new UserBindThird();
        userBindThird.setUserId(user.getId());
        userBindThird.setImgUrl(wxTokenVO.getHeadimgUrl());
        userBindThird.setNickname(wxTokenVO.getNickName());
        userBindThird.setBindStatus(1);
        userBindThird.setOpenId(wxTokenVO.getOpenId());
        userBindThird.setAccessToken(wxTokenVO.getAccessToken());
        userBindThird.setSessionKey(wxTokenVO.getSessionKey());
        userBindThird.setBindType(registerDTO.getJavaBindType());
        userBindThird.setRegDate(DateUtil.toyyyy_MM_dd(now));
        userBindThird.setRegTime(DateUtil.toHH_mm_ss(now));
        userBindThird.setAddTime(now);
        userBindThird.setUpdateTime(now);
        userBindThird.setSysCnl(sysCnl);
        userBindThird.setBindId(wxTokenVO.getUnionid());
        iShopUserBindThirdService.save(userBindThird);

        user.setHeadImgUrl(wxTokenVO.getHeadimgUrl());
        user.setName(wxTokenVO.getNickName());
        user.setGender(wxTokenVO.getSex());
        user.setNickname(wxTokenVO.getNickName());
        updateById(user);
        return loginRegisterResultVO;
    }

    @Transactional
    @Override
    public LoginRegisterResultVO bindByWX(String appVersion, RegisterDTO registerDTO, HttpServletRequest request){

        WxTokenVO wxTokenVO = (WxTokenVO) RedisUtil.get(registerDTO.getTokenId());
        if (null == wxTokenVO) {
            throw new ShopException("11111", "tokenId已经失效");
        }
        User user = getOne(new QueryWrapper<User>().eq("mobile", registerDTO.getMobile()));

        String bindType;
        if (UserServiceImpl.BIND_SOURCE_MP.equals(registerDTO.getSource())) {
            bindType = ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue();
        } else {
            if (ConstantsEnum.USER_BIND_THIRD_TYPE_H5.stringValue().equalsIgnoreCase(registerDTO.getSource())) {
                bindType = ConstantsEnum.USER_BIND_THIRD_TYPE_H5.value().toString();
            } else {
                bindType = ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.value().toString();
            }
        }
        registerDTO.setJavaBindType(bindType);

        String sysCnl = null;
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(userAgent)) {
            if (userAgent.contains("IOS") || userAgent.contains("ios")) {
                sysCnl = "IOS";
            } else if (userAgent.contains("android") || userAgent.contains("ANDROID")) {
                sysCnl = "ANDROID";
            }
        }
        if (BIND_SOURCE_MP.equals(wxTokenVO.getSource())) {// 小程序
            if (user == null) {
                return registerNewWXUser(appVersion, wxTokenVO, registerDTO, sysCnl);
            } else {// 已手机号信息为主
                //return new HashMap<>();
                return new LoginRegisterResultVO();
            }
        } else {// 普通微信

            if (registerDTO.getSureBind().equals(1)) {// 确认绑定
                LocalDateTime now = LocalDateTime.now();
                UserBindThird userBindThird = new UserBindThird();
                userBindThird.setUserId(user.getId());
                userBindThird.setImgUrl(wxTokenVO.getHeadimgUrl());
                userBindThird.setNickname(wxTokenVO.getNickName());
                userBindThird.setBindId(wxTokenVO.getUnionid());
                userBindThird.setBindStatus(1);
                userBindThird.setOpenId(wxTokenVO.getOpenId());
                userBindThird.setAccessToken(wxTokenVO.getAccessToken());
                userBindThird.setSessionKey(wxTokenVO.getSessionKey());
                userBindThird.setBindType(registerDTO.getJavaBindType());
                userBindThird.setRegDate(DateUtil.toyyyy_MM_dd(now));
                userBindThird.setRegTime(DateUtil.toHH_mm_ss(now));
                userBindThird.setAddTime(now);
                userBindThird.setUpdateTime(now);
                userBindThird.setSysCnl(sysCnl);
                iShopUserBindThirdService.save(userBindThird);

                //return new HashMap<>();
                return new LoginRegisterResultVO();
            } else {// 首次绑定
                if (user == null) {// 手机号不存在，直接注册绑定
                    // 邀请码
                    return registerNewWXUser(appVersion, wxTokenVO, registerDTO, sysCnl);//
                } else {// 手机号存在
                    UserBindThird userBindThird = iShopUserBindThirdService.getOne(new QueryWrapper<UserBindThird>()
                            .eq("bind_type", bindType).eq("user_id", user.getId()));
                    if (userBindThird != null) {// 手机号已绑定微信，返回提示
                        throw new ShopException(10039);
                    } else {// 返回确认信息,当确认后(sureBind=1)再进行绑定
                        throw new ShopException(10025);
                    }
                }
            }
        }
    }

    @Override
    public List<User> xxx() {
        return baseMapper.xxx();
    }

    @Override
    public UserLoginVO bindMpUnionid(RegisterDTO registerDTO) {

        SessionKeyOpenIdDTO sessionKeyOpenIdDTO = iWeixinIndependenceService.mpSessionKeyDTO(registerDTO.getMpCode(), registerDTO.getSessionKeyStr());
        String session_key = sessionKeyOpenIdDTO.getSession_key();
        if (StringUtils.isBlank(session_key)) {
            log.error("sessionkey无效|{}|{}|{}", registerDTO.getMpCode(), registerDTO.getEncryptedData(), registerDTO.getIv());
            throw new ShopException("sessionkey无效");
        }
        WxMaUserInfo userInfo = WxMaUserInfo.fromJson(WxMaCryptUtils.decryptAnotherWay(session_key, registerDTO.getEncryptedData(), registerDTO.getIv()));//wxMaService.getUserService().getUserInfo(session_key, registerDTO.getEncryptedData(), registerDTO.getIv());
        if (userInfo == null || StringUtils.isBlank(userInfo.getUnionId())) {
            log.error("获取用户信息失败|{}|{}|{}", session_key, registerDTO.getEncryptedData(), registerDTO.getIv());
            throw new ShopException("获取用户信息失败");
        }
        log.info("小程序用户信息解密|{}", JSON.toJSONString(userInfo));
        User user = getOne(new QueryWrapper<User>().eq("mobile", registerDTO.getMobile()).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));
        UserBindThird userBindThird = iShopUserBindThirdService.findOne(user.getId(), registerDTO.getSysCnl());
        if (userBindThird == null) {
            LocalDateTime now = LocalDateTime.now();
            userBindThird = new UserBindThird();
            userBindThird.setUserId(user.getId());
            userBindThird.setBindId(userInfo.getUnionId());
            userBindThird.setOpenId(userInfo.getOpenId());
            userBindThird.setImgUrl(userInfo.getAvatarUrl());
            userBindThird.setNickname(userInfo.getNickName());
            userBindThird.setBindType(ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue());
            userBindThird.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
            userBindThird.setBindStatus(1);
            userBindThird.setSysCnl("MP");
            userBindThird.setRegDate(DateUtil.toyyyy_MM_dd(now));
            userBindThird.setRegTime(DateUtil.toHH_mm_ss(now));
            userBindThird.setAddTime(now);
            userBindThird.setUpdateTime(now);
            iShopUserBindThirdService.save(userBindThird);
        } else {
            LocalDateTime now = LocalDateTime.now();
            userBindThird.setBindId(userInfo.getUnionId());
            userBindThird.setOpenId(userInfo.getOpenId());
            userBindThird.setImgUrl(userInfo.getAvatarUrl());
            userBindThird.setNickname(userInfo.getNickName());
            userBindThird.setUpdateTime(now);
            iShopUserBindThirdService.updateById(userBindThird);
        }
        return userLoginData(user, true);
    }

    @Override
    public Map<String, Object> needUnionid(String sysCnl, Long uid, String mobile) {
        // H5、WEB、WX-APPLET、WX-PUBLIC
        if (!(ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue().equalsIgnoreCase(sysCnl)
                || ConstantsEnum.USER_BIND_THIRD_TYPE_H5.stringValue().equalsIgnoreCase(sysCnl))) {
            return null;
        }
        // 提示要绑定unionid
        UserBindThird one = iShopUserBindThirdService.findOne(uid, sysCnl);
        if (one == null || StringUtils.isBlank(one.getBindId()) || StringUtils.isBlank(one.getOpenId())) {
            HashMap<String, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("mobile", mobile);
            return objectObjectHashMap;
        }
        return null;
    }

    @Override
    public IPage wxUserList(IPage<WxUserDTO> page, WxUserDTO params) {
        page.setRecords(baseMapper.wxUserList(page, params));
        return page;
    }

    /**
     * 通过管理平台创建用户
     *
     * @param adminId
     * @param adminUserDTO
     */
    @Override
    public AdminCheckUserDTO managementCheckUser(Long adminId, AdminUserDTO adminUserDTO, HttpServletRequest request) {
        log.info("|管理平检测用户|操作人:{},请求报文:{}", adminId, adminUserDTO);
        AdminCheckUserDTO checkUserDTO = new AdminCheckUserDTO();
        int xfyinliHasFalg = 0;
        int zhuanboHasFalg = 0;
        String mobile = adminUserDTO.getMobile();
        String areaCode = adminUserDTO.getAreaCode();
        String inviteCode = adminUserDTO.getInviteCode();
        // 校验幸福引力用户是否存在
        long timeFlag = System.currentTimeMillis();
        AdminUserDTO yinliUserDTO = iYinLiUserService.getUserByMobileAndAreaCode(adminUserDTO);
        if (yinliUserDTO != null && yinliUserDTO.getId() != null) {
            xfyinliHasFalg = 1;
        }
        log.info("|管理平检测用户|校验幸福引力用户是否存在|耗时:{}", System.currentTimeMillis() - timeFlag);

        // 获取token信息
        timeFlag = System.currentTimeMillis();
        String adminToken = request.getHeader("Admin-Token");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mobile", mobile);
        paramMap.put("areaCode", areaCode);
        paramMap.put("inviteCode", inviteCode);
        List<Integer> ptLevelList = mliveClientUtil.checkUser(authConfig.getMliveUrl(), paramMap, adminToken);
        checkUserDTO.setPtLevels(ptLevelList);
        log.info("|管理平检测用户|调用php获取等级及校验信息|耗时:{}", System.currentTimeMillis() - timeFlag);

        // 校验赚播用户是否
        User user = this.getOne(new QueryWrapper<User>().eq("mobile", mobile).eq("area_code", areaCode).eq("deleted", 0).eq("status", 1));
        if (user != null) {
            zhuanboHasFalg = 1;
        }
        // 校验邀请码是否存在
        User inviteCodeUser = this.getOne(new QueryWrapper<User>().eq("invite_code", inviteCode).eq("deleted", 0).eq("status", 1));
        if (inviteCodeUser == null) {
            throw new ShopException("邀请码无效");
        }

        int modifyHigherUps = 1;
        int modifyPtLevel = 0;
        int modifyGoodsType = 0;
        int isNewUser = 0;
        if (zhuanboHasFalg == 1 && xfyinliHasFalg == 1) {

        } else if(zhuanboHasFalg == 1 && xfyinliHasFalg == 0){

        } else if (zhuanboHasFalg == 0 && xfyinliHasFalg == 1) {
            modifyPtLevel = 1;
        } else if (zhuanboHasFalg == 0 && xfyinliHasFalg == 0) {
            modifyPtLevel = 1;
            isNewUser = 1;
        }

        checkUserDTO.setModifyHigherUps(modifyHigherUps);
        checkUserDTO.setModifyPtLevel(modifyPtLevel);
        checkUserDTO.setModifyGoodsType(modifyGoodsType);
        checkUserDTO.setIsNewUser(isNewUser);
        checkUserDTO.setProxyType(1);
        checkUserDTO.setGoodsType(1);
        checkUserDTO.setGoodsModel(1);
        checkUserDTO.setPayType(1);
        checkUserDTO.setPrice(new BigDecimal(499));
        checkUserDTO.setPoint(499);

        return checkUserDTO;
    }


    /**
     * 通过管理平台创建用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> managementCreateUser(Long adminId, AdminUserDTO adminUserDTO, HttpServletRequest request) {
        log.info("|管理平台创建用户|操作人:{},请求报文:{}", adminId, adminUserDTO);
        String mobile = adminUserDTO.getMobile();
        String areaCode = adminUserDTO.getAreaCode();
        Integer ptLevel = adminUserDTO.getPtLevel();
        String inviteCode = adminUserDTO.getInviteCode();
        Integer payType = adminUserDTO.getPayType();
        String remark = adminUserDTO.getRemark();
        String msgCode = "0";
        Integer point = adminUserDTO.getPoint();
        // 校验admin积分是否充足
        UserIncome userIncome = iUserIncomeService.getByUserId(adminId);
        if (userIncome == null) {
            throw new ShopException("无用户积分");
        }
        Integer usablePoint = userIncome.getUsablePoint();
        if (point > usablePoint) {
            throw new ShopException("积分不足，请联系公司进行充值");
        }
        // 用户校验
        String adminToken = request.getHeader("Admin-Token");
//        StringBuffer paramSb = new StringBuffer();
//        paramSb.append("mobile=").append(mobile);
//        paramSb.append("&areaCode=").append(areaCode);
//        paramSb.append("&inviteCode=").append(inviteCode);
//        MliveClientUtil.checkUser(authConfig.getMliveUrl(), paramSb.toString(), adminToken);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mobile", mobile);
        paramMap.put("areaCode", areaCode);
        paramMap.put("inviteCode", inviteCode);
        List<Integer> ptLevelList = mliveClientUtil.checkUser(authConfig.getMliveUrl(), paramMap, adminToken);

        // 校验面膜用户是否存在
        UserLoginVO userLoginVO = null;
        User user = this.getOne(new QueryWrapper<User>().eq("mobile", mobile).eq("area_code", areaCode));
        LoginRegisterResultVO loginRegisterResultVO = null;
        if (user == null) {
            // 如果用户不存在,创建新用户
            RedisUtil.set(mobile, msgCode, 30);
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setMobile(mobile);
            registerDTO.setCode(msgCode);
            registerDTO.setInviteCode(inviteCode);
            try {
                // 公共注册
                loginRegisterResultVO = this.registerEntrance(registerDTO, true);
                userLoginVO = loginRegisterResultVO.getUserLoginVO();
                user = this.getById(userLoginVO.getId());
            } catch (Exception e) {
                log.error("注册异常:{}", e);
                if (e instanceof ShopException) {
                    throw new ShopException(e.getMessage());
                }
                throw new ShopException(10031);
            }
        } else {
            User inviteUser = this.getOne(new QueryWrapper<User>().eq("invite_code", inviteCode));
            UserInvite oldUserInvite = iUserInviteService.getById(user.getId());
            if (inviteUser != null) {
                if (oldUserInvite != null && !inviteUser.getId().equals(oldUserInvite.getPid())) {
                    user.setInviteUpUserId(inviteUser.getId());
                }
            }
        }

        // 记录等级变化记录表
        Long pid = null;
        User pUser = this.getOne(new QueryWrapper<User>().eq("invite_code", inviteCode));
        if (pUser != null) {
            pid = pUser.getId();
        }
        LevelChangeRecode levelChangeRecode = LevelChangeRecode.builder().userId(user.getId()).pid(pid).ptLevel(ptLevel).oldPtLevel(user.getPtLevel())
                .payType(payType).price(BigDecimal.valueOf(point)).remark(remark).operatorId(adminId).build();
        iLevelChangeRecodeService.save(levelChangeRecode);

        //用户等级=0，则不生成积分
        if (adminUserDTO.getPtLevel() == PtLevelType.ORDINARY.getId()) {
        	// MQ用户信息更新
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
        	return null;
        }
        List<Integer> typeSplit = Arrays.asList(DepositOrderTypeSplitEnum.TYPE_SPLIT_1.getId());

        // 扣减积分
        Long userId = user.getId();
        AdminPointDTO adminPointDTO = new AdminPointDTO();
        adminPointDTO.setUserId(adminId);
        adminPointDTO.setFromUserId(userId);
        adminPointDTO.setPoint(point);
        adminPointDTO.setPointType(PointTypeEnum.PAY.getId());
        adminPointDTO.setOperateType(UserIncomeOperateType.SUBSTRACT.getId());
        adminPointDTO.setTradeCode(TradeCode.CONSUMER.getId());
        adminPointDTO.setBusiType(DepositOrderBusiTypeEnum.MEMBERSHIP_PACKAGE.getId());
        adminPointDTO.setTypeSplit(typeSplit);
        adminPointDTO.setPtLevel(ptLevel);
        adminPointDTO.setAdminId(adminId);
        iUserIncomeService.depositPoint(adminPointDTO);

        // MQ用户信息更新
        if (loginRegisterResultVO != null) {
            this.afterRegister(loginRegisterResultVO);
        }
        if (loginRegisterResultVO == null || userLoginVO == null) {
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
        }

        // MQ订单队列
        Map<String, Object> profitOrderMap = new HashMap<>();
        profitOrderMap.put("orderNo", adminPointDTO.getDepositNo());
        profitOrderMap.put("orderType", DepositOrderTypeSplitEnum.TYPE_SPLIT_1.getId());
        profitOrderMap.put("typeSplit", typeSplit);
        return profitOrderMap;
    }

    /**
     * 根据手机号或者授权号获取用户信息
     *
     * @param mobile
     * @param areaCode
     * @param authNo
     * @return
     */
    @Override
    public User getByIdOrMobileOrAuthNo(Long userId, String mobile, String areaCode, String authNo) {
        return baseMapper.getByIdOrMobileOrAuthNo(userId, mobile, areaCode, authNo);
    }

    /**
     * 修改用户等级
     *
     * @param adminId
     * @param adminUserDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> managementEditPtlevel(Long adminId, AdminUserDTO adminUserDTO) {
        log.info("修改用户等级,操作人:{}, 请求报文:{}", adminId, adminUserDTO);
        // 更新用户等级
        Long id = adminUserDTO.getId();
        Integer ptLevel = adminUserDTO.getPtLevel();
        Integer payType = adminUserDTO.getPayType();
        User user = this.getById(id);
        if (user == null) {
            throw new ShopException("用户信息不存在");
        }
        Integer oldPtLevel = user.getPtLevel();
        if (oldPtLevel >= ptLevel) {
            throw new ShopException("当前用户等级是或高于" + PtLevelType.toName(ptLevel));
        }

        log.info("修改用户等级,获取积分(调用php)");
        // 获取积分 - 调用php
        InvestorsPriceDTO investors = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3");
        // 如果未普通用户,需要拆单
        BigDecimal levelPrice = BigDecimal.ZERO;
        List<Integer> typeSplit = new ArrayList<>();

        // 根据等级进行积分计算
        if (PtLevelType.ORDINARY.getId() == oldPtLevel && oldPtLevel + 1 != ptLevel) {
            levelPrice = levelPrice.add(investors.getLevelPrice(oldPtLevel + 1));
            typeSplit.add(oldPtLevel + 1);
        }
        levelPrice = levelPrice.add(investors.getLevelPrice(ptLevel));
        typeSplit.add(ptLevel);

        // 校验admin积分是否充足
        UserIncome userIncome = iUserIncomeService.getByUserId(adminId);
        if (userIncome == null) {
            throw new ShopException("无用户积分");
        }
        Integer usablePoint = userIncome.getUsablePoint();
        if (levelPrice.intValue() > usablePoint) {
            throw new ShopException("积分不足，请联系公司进行充值");
        }

        // 记录等级变化记录表
        UserInvite userInvite = iUserInviteService.getById(user.getId());
        LevelChangeRecode levelChangeRecode = LevelChangeRecode.builder().userId(user.getId()).pid(userInvite.getPid()).ptLevel(ptLevel)
                .oldPtLevel(oldPtLevel).payType(payType).price(BigDecimal.valueOf(levelPrice.intValue())).operatorId(adminId).build();
        iLevelChangeRecodeService.save(levelChangeRecode);

        // 扣减积分
        Integer point = adminUserDTO.getPoint();
        AdminPointDTO adminPointDTO = new AdminPointDTO();
        adminPointDTO.setUserId(adminId);
        adminPointDTO.setFromUserId(user.getId());
        adminPointDTO.setPoint(levelPrice.intValue());
        adminPointDTO.setPointType(PointTypeEnum.PAY.getId());
        adminPointDTO.setOperateType(UserIncomeOperateType.SUBSTRACT.getId());
        adminPointDTO.setTradeCode(TradeCode.CONSUMER.getId());
        adminPointDTO.setBusiType(DepositOrderBusiTypeEnum.MEMBERSHIP_PACKAGE.getId());
        adminPointDTO.setAdminId(adminId);
        adminPointDTO.setPtLevel(ptLevel);
        adminPointDTO.setTypeSplit(typeSplit);
        iUserIncomeService.depositPoint(adminPointDTO);

        // MQ用户信息更新
        user.setInviteUpUserId(0L);
        iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
        // MQ订单队列
        Map<String, Object> profitOrderMap = new HashMap<>();
        profitOrderMap.put("orderNo", adminPointDTO.getDepositNo());
        profitOrderMap.put("orderType", ptLevel);
        profitOrderMap.put("typeSplit", typeSplit);
        return profitOrderMap;
    }

    /**
     * 修改邀请上级
     *
     * @param adminId
     * @param adminUserDTO
     * @return
     */
    @Override
    public UserUpgradePointVO managementUpgradePoint(Long adminId, AdminUserDTO adminUserDTO) {
        UserUpgradePointVO upgradePointVO = new UserUpgradePointVO();
        Long userId = adminUserDTO.getId();
        User user = this.getById(userId);
        if (user == null) {
            throw new ShopException(10007);
        }
        Integer oldPtLevel = user.getPtLevel();
        // 如果未普通用户,需要拆单
        BigDecimal levelPrice;
        int maxPtLevel = PtLevelType.CC.getId();
        InvestorsPriceDTO investors = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3");
        Map<Integer, Integer> pointMap = new HashMap<>();
        for (int i = oldPtLevel + 1; i < maxPtLevel; i++) {
            levelPrice = BigDecimal.ZERO;
            if (PtLevelType.ORDINARY.getId() == oldPtLevel && PtLevelType.VIP.getId() != i) {
                levelPrice = levelPrice.add(investors.getLevelPrice(oldPtLevel + 1));
            }
            levelPrice = levelPrice.add(investors.getLevelPrice(i));
            pointMap.put(i, levelPrice.intValue());
        }
        upgradePointVO.setPayType(1);
        upgradePointVO.setLevelPointInfo(pointMap);
        return upgradePointVO;
    }

}
