package com.zhuanbo.shop.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.annotation.ResponseLog;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.OrderTypeEnum;
import com.zhuanbo.core.constants.RealedStatus;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.constants.TradeCode;
import com.zhuanbo.core.constants.WithdrOrderStatusEnum;
import com.zhuanbo.core.dto.PayCardBindDTO;
import com.zhuanbo.core.dto.WithdrDicDTO;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBindThird;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.core.enums.BankCode;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.AESCoder;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.core.util.SpringContextUtil;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.wx.service.IThirdService;
import com.zhuanbo.external.service.wx.vo.AccessTokenThirdVO;
import com.zhuanbo.external.service.wx.vo.UserInfoThirdVO;
import com.zhuanbo.service.service.IAppVersionService;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IPayCardBindService;
import com.zhuanbo.service.service.IPayDictionaryService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserBindThirdService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service.IWithdrOrderService;
import com.zhuanbo.shop.api.dto.req.WithdrOrderReqDTO;
import com.zhuanbo.shop.api.dto.resp.WithdrOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 提现
 */
@RestController
@RequestMapping("/shop/mobile/withdr")
@Slf4j
@ResponseLog
public class MobileWithdrController {

	@Autowired
    private AuthConfig authConfig;
    @Autowired
    private IWithdrOrderService iWithdrOrderService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private IUserBindThirdService iUserBindThirdService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    private IAppVersionService iAppVersionService;
    @Autowired
    private IPayDictionaryService iPayDictionaryService;
    @Autowired
    private IPayCardBindService iPayCardBindService;
    @Autowired
    private IDepositOrderService iDepositOrderService;

    /**
     * 提现银行查询
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/bank/index")
    public Object bankIndex(@LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO,
                            HttpServletRequest request) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_QUERY_WITHDR_BANK.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("userId", userId);
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());
        params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        // 绑定的银行卡（支、微信）列表
        JSONObject bankList = requestResult(params);

        Map<String, Object> backMap = new HashMap<>();
        if (bankList != null) {

            JSONArray cardBindList = bankList.getJSONArray("cardBindList");
            for (int i = 0; i < cardBindList.size(); i++) {
                JSONObject jsonObject = cardBindList.getJSONObject(i);
                if (StringUtils.isBlank(jsonObject.getString("bankCardNo"))) {
                    jsonObject.put("bindType", 0);
                } else {
                    jsonObject.put("bindType", 1);
                }

                // 如果不是WX-PUBLIC,不显示:微信银行卡
                String bankCode = jsonObject.getString("bankCode");
                if (BankCode.WEIXIN.getId().equals(bankCode)) {
                    if ("IOS".equals(withdrOrderReqDTO.getSysCnl()) || "ANDROID".equals(withdrOrderReqDTO.getSysCnl())) {
                        cardBindList.remove(i);
                    }
                }
            }

            String version = request.getHeader("X-MPMALL-APPVer");
            if (StringUtils.isBlank(version)) {
                version = request.getHeader("X-MP-APPVer");
            }

            log.info("version={}", version);
            if (!StringUtils.isBlank(version)) {
                if (version.contains("(")) {// IOS旧版问题
                    version = version.substring(0, version.indexOf("("));
                }
                if (version.length() > 3) {
                    version = version.substring(0, 3);
                }
                if ("1.4".compareTo(version) > 0) {
                    cardBindList.removeIf(x -> {
                        JSONObject value = (JSONObject) x;
                        Integer bankType = value.getInteger("bankType");
                        if (null != bankType && bankType == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    });

                }
            }

            backMap.put("bankList", cardBindList);
        } else {
            backMap.put("bankList", new ArrayList<>());
        }

        log.info("提现银行查询" + backMap);
        return ResponseUtil.ok(backMap);
    }

    /**
     * 提现银行绑定查询
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/bank/bind/index")
    public Object bindIndex(@LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_QUERY_CARD_BIND.String());
        params.put("userId", userId);
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());
        params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        // 绑定的银行卡（支、微信）列表
        JSONObject cardBindList = requestResult(params);
        // 用户余额
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_QUERY_BALANCE.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        JSONObject userBalance = requestResult(params);

        Map<String, Object> backMap = new HashMap<>();
        ArrayList<Object> ls = new ArrayList<>();
        if (cardBindList != null) {

            JSONArray cardBindListJSONArray = cardBindList.getJSONArray("cardBindList");
            for (int i = 0; i < cardBindListJSONArray.size(); i++) {
                JSONObject jsonObject = cardBindListJSONArray.getJSONObject(i);
                // 这里就是绑定的数据
                jsonObject.put("bindType", 1);

                // 如果不是WX-PUBLIC,不显示:微信银行卡
                String bankCode = jsonObject.getString("bankCode");
                if (BankCode.WEIXIN.getId().equals(bankCode)) {
                    if ("IOS".equals(withdrOrderReqDTO.getSysCnl()) || "ANDROID".equals(withdrOrderReqDTO.getSysCnl())) {
                        cardBindListJSONArray.remove(i);
                    }
                }
            }
            backMap.put("cardBindList", cardBindList.getJSONArray("cardBindList"));
        } else {
            backMap.put("cardBindList", ls);
        }
        if (userBalance != null) {
            backMap.put("bal", userBalance);
        } else {
            backMap.put("bal", new HashMap<>());
        }

        List<BigDecimal> priceList = Arrays.stream(iDictionaryService.findForString("price", "amountList").split(","))
                .map(x -> new BigDecimal(x)).collect(Collectors.toList());
        backMap.put("priceList", priceList);

        log.info("提现银行绑定查询" + backMap);
        return ResponseUtil.ok(backMap);
    }

    /**
     * 提现绑卡
     *
     * @return
     */
    @PostMapping("/bank/bind")
    public Object bind(@LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO, @RequestBody AppIdKeyDTO appIdKeyDTO) throws Exception {

        User user = iUserService.getById(userId);

        log.info("withdrOrderReqDTO === {}", JSON.toJSONString(withdrOrderReqDTO));

        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_CARD_BIND.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("userId", userId);
        params.put("bankCode", withdrOrderReqDTO.getBankCode());
        params.put("bankCardType", withdrOrderReqDTO.getBankCardType());
        params.put("tradeType", withdrOrderReqDTO.getTradeType());
        params.put("sysCnl", withdrOrderReqDTO.getSysCnl());
        params.put("clientIp", withdrOrderReqDTO.getClientIp());
        params.put("timestamp", withdrOrderReqDTO.getTimestamp());
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());

        if (ConstantsEnum.WEIXIN.stringValue().equalsIgnoreCase(withdrOrderReqDTO.getBankCode())
                || ConstantsEnum.ALIPAY.stringValue().equalsIgnoreCase(withdrOrderReqDTO.getBankCode())) {
            IThirdService iThirdService = (IThirdService) SpringContextUtil
                    .getBean(withdrOrderReqDTO.getBankCode().toLowerCase() + "ThirdService");
            appIdKeyDTO.setOperType(3);
            AccessTokenThirdVO wxAccessTokenVO = iThirdService.getAccessTokenVO(appIdKeyDTO, withdrOrderReqDTO.getCode(),
                    withdrOrderReqDTO.getPlatform());
            if (wxAccessTokenVO == null || StringUtils.isBlank(wxAccessTokenVO.getOpenId())) {
                return ResponseUtil.result(10034);
            }

            String aesKey = iDictionaryService.findForString("SecretKey", "AES");
            String aesIv = iDictionaryService.findForString("SecretKey", "IV");

            params.put("bankCardNo", AESCoder.encrypt(wxAccessTokenVO.getOpenId(), aesKey, aesIv));
            UserInfoThirdVO userInfoThirdVO = iThirdService.getUserInfo(appIdKeyDTO, wxAccessTokenVO.getAccessToken(),
                    wxAccessTokenVO.getOpenId());
            if (userInfoThirdVO != null) {
                params.put("bankCardName", userInfoThirdVO.getNickName());
            } else {
                UserBindThird userBindThird = iUserBindThirdService
                        .getOne(new QueryWrapper<UserBindThird>().eq("open_id", wxAccessTokenVO.getOpenId()));
                if (userBindThird != null && userBindThird.getUserId() != null) {
                    if (user != null) {
                        params.put("bankCardName", user.getNickname());
                    }
                }
            }
        } else {

            //用户实名认证
            if (user.getRealed() < RealedStatus.WEAK_REAL.getId()) {
                Map<String, Object> realedNameMap = new HashMap<String, Object>();
                realedNameMap.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_USER_REAL_NAME.String());
                realedNameMap.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
                realedNameMap.put("userId", userId);
                realedNameMap.put("name", withdrOrderReqDTO.getBankCardName());
                realedNameMap.put("cardNo", withdrOrderReqDTO.getCardNo());
                realedNameMap.put("cardType", withdrOrderReqDTO.getCardType());
                realedNameMap.put("imgFront", withdrOrderReqDTO.getImgFront());
                realedNameMap.put("imgBack", withdrOrderReqDTO.getImgBack());
                realedNameMap.put("realSource", 3);
                realedNameMap.put("realType", 0);
                realedNameMap.put("sysCnl", withdrOrderReqDTO.getSysCnl());
                realedNameMap.put("clientIp", withdrOrderReqDTO.getClientIp());
                realedNameMap.put("timestamp", withdrOrderReqDTO.getTimestamp());
                realedNameMap.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
                realedNameMap.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());
                requestResult(realedNameMap);
            }

            params.put("mobile", withdrOrderReqDTO.getMobile());
            params.put("bankCardName", withdrOrderReqDTO.getBankCardName());
            params.put("bankNo", withdrOrderReqDTO.getBankNo());
            params.put("bankProv", withdrOrderReqDTO.getBankProv());
            params.put("bankCity", withdrOrderReqDTO.getBankCity());
            params.put("cardNo", withdrOrderReqDTO.getCardNo());
            params.put("cardType", withdrOrderReqDTO.getCardType());
            params.put("bankCardNo", withdrOrderReqDTO.getBankCardNo());
            params.put("bankCardImgFront", withdrOrderReqDTO.getBankCardImgFront());
        }

        JSONObject bind = requestResult(params);
        if (bind == null) {
            return ResponseUtil.fail(11203);
        }

        return ResponseUtil.ok(bind);
    }

    /**
     * 提现绑卡短信
     *
     * @return
     */
    @PostMapping("/bank/bind/sms")
    public Object bindSms(@LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO) throws Exception {

        log.info("withdrOrderReqDTO === " + withdrOrderReqDTO.getBankCardType());

        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_CARD_BIND_SMS.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("agrNo", withdrOrderReqDTO.getAgrNo());
        params.put("smsOrderNo", withdrOrderReqDTO.getSmsOrderNo());
        params.put("sysCnl", withdrOrderReqDTO.getSysCnl());
        params.put("clientIp", withdrOrderReqDTO.getClientIp());
        params.put("timestamp", withdrOrderReqDTO.getTimestamp());
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());
        params.put(ReqResEnum.TIMESTAMP.String(), withdrOrderReqDTO.getTimestamp());

        log.info("bindSms_request=== {} ", JacksonUtil.objTojson(params));

        JSONObject bind = requestResult(params);
        log.info("bindSms_respose=== {}", JacksonUtil.objTojson(bind));

        return ResponseUtil.ok(bind);
    }

    /**
     * 提现绑卡确认
     *
     * @return
     */
    @PostMapping("/bank/bind/confirm")
    public Object bindConfirm(@LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO)
            throws Exception {
        log.info("|提现绑卡确认|用户id:{}, 请求报文:{}", userId, withdrOrderReqDTO);
        User user = iUserService.getById(userId);

        log.info("withdrOrderReqDTO === " + withdrOrderReqDTO.getBankCardType());

        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_CARD_BIND_CONFIRM.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("agrNo", withdrOrderReqDTO.getAgrNo());
        params.put("smsOrderNo", withdrOrderReqDTO.getSmsOrderNo());
        params.put("smsCode", withdrOrderReqDTO.getSmsCode());
        params.put("sysCnl", withdrOrderReqDTO.getSysCnl());
        params.put("clientIp", withdrOrderReqDTO.getClientIp());
        params.put("timestamp", withdrOrderReqDTO.getTimestamp());
        params.put("userId", userId);
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());

        log.info("bindConfirm_request=== " + params);

        JSONObject bind = requestResult(params);
        log.info("bindConfirm_response=== " + params);

        // 如果是银行卡，同时用户未实名，则标记成已实名
        if ("01".equals(bind.getString("bankCardType")) && user.getRealed() < RealedStatus.MIDDLE_REAL.getId()) {
            user.setRealed(RealedStatus.MIDDLE_REAL.getId());
            user.setCardType(bind.getInteger("cardType"));
            user.setCardNo(bind.getString("cardNo"));
            user.setName(bind.getString("bankCardName"));
            iUserService.updateById(user);
            RedisUtil.del(ConstantsEnum.REDIS_USER_MPMALL.stringValue() + userId);
        }

        return ResponseUtil.ok(bind);
    }

    /**
     * 提现解绑
     *
     * @param userId
     * @param withdrOrderReqDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/bank/unbind")
    public Object unbind(@LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO) throws Exception {
        log.info("|提现解绑|用户id:{}, 请求报文:{}", userId, withdrOrderReqDTO);
        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_UN_CARD_BIND.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("userId", userId);
        params.put("agrNo", withdrOrderReqDTO.getAgrNo());
        params.put("bankCode", withdrOrderReqDTO.getBankCode());
        params.put("bankCardNo", withdrOrderReqDTO.getBankCardNo());
        params.put("clientIp", withdrOrderReqDTO.getClientIp());
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());
        params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());

        JSONObject unbind = requestResult(params);
        return ResponseUtil.ok();
    }

    /**
     * 提现申请
     *
     * @param userId
     * @param withdrOrderReqDTO
     * @return
     */
    @PostMapping("/apply")
    public Object apply(@RequestHeader(name = "X-MPMALL-APPVer", required = false) String appVersion,
                        @LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO) {
        log.info("|提现申请|用户id:{}, 接收到请求报文:{}", userId, withdrOrderReqDTO);
        User user = iUserService.getById(userId);
        if (user == null) {
            return ResponseUtil.result(10007);
        }
        if (StringUtils.isNotBlank(appVersion)) {
            int newForTX = iAppVersionService.isNewForTX(withdrOrderReqDTO.getPlatform(), appVersion);
            if (newForTX != 1) {
                return ResponseUtil.result(72000);
            }
        }

        if (user.getPtLevel() > 0) {
        	int count = iDepositOrderService.count(
            		new QueryWrapper<DepositOrder>()
            		.eq("user_id", userId)
            		.ge("order_type", OrderTypeEnum.BUY_ORDER_TYPE_1.getValue())
            		.eq("trade_code", TradeCode.CONSUMER.getId())
            		.eq("busi_type", ConstantsEnum.DEPOSIT_BUSI_TYPE_06.stringValue())
            		.eq("order_status", OrderStatus.SUCCESS.getId())
            		);
        	//保留以下注释代码
            if (count <= 0) {
            	log.error("用户id:{},ptLevel:{},count:{} 你的等级尚未激活，请激活后再进行提现操作", userId, user.getPtLevel(), count);
            	return ResponseUtil.result(11206, "你的等级尚未激活，请激活后再进行提现操作");
            }
        }

        // bankCode获取
        String bankCode = withdrOrderReqDTO.getBankCode();
        if (StringUtils.isBlank(withdrOrderReqDTO.getBankCode()) || StringUtils.isNotBlank(withdrOrderReqDTO.getAgrNo())) {
            PayCardBindDTO cardBindDTO = new PayCardBindDTO();
            cardBindDTO.setUserId(userId);
            cardBindDTO.setAgrNo(withdrOrderReqDTO.getAgrNo());
            cardBindDTO = iPayCardBindService.getCardBindByAgrNo(cardBindDTO);
            bankCode = cardBindDTO.getBankCode();
        }

        // 提现限制校验
        WithdrDicDTO withdrDic = iPayDictionaryService.getWithdrDic();
        BigDecimal price = withdrOrderReqDTO.getPrice();
        iWithdrOrderService.withdrApplyCheck(userId, price, withdrDic, bankCode);

        // 判断如果微信支付不进行审核
        String orderStatus = WithdrOrderStatusEnum.AUDIT_WAIT.getId();
        if (BankCode.WEIXIN.getId().equals(bankCode)) {
            orderStatus = WithdrOrderStatusEnum.WAIT.getId();
        }

        LocalDateTime now = LocalDateTime.now();
        String orderNo;
        WithdrOrderDTO withdrOrderDTO = new WithdrOrderDTO();
        try {
            orderNo = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + iSeqIncrService.nextVal("withdr_order_no", 8, Align.LEFT);

            // 计算实际提现金额
            Long withdrRatioLong = withdrDic.getCommisionRatio();
            BigDecimal withdrRatio = BigDecimal.valueOf(withdrRatioLong).multiply(new BigDecimal(0.001)).setScale(3, BigDecimal.ROUND_HALF_UP);
            BigDecimal withdrPrice = price.subtract(price.multiply(withdrRatio)).setScale(2, BigDecimal.ROUND_DOWN);

            WithdrOrder withdrOrder = new WithdrOrder();
            withdrOrder.setUserId(userId);
            withdrOrder.setMercId(withdrOrderReqDTO.getMercId());
            withdrOrder.setPlatform(withdrOrderReqDTO.getPlatform());
            withdrOrder.setOrderNo(orderNo);
            withdrOrder.setOrderDate(DateUtil.toyyyy_MM_dd(now));
            withdrOrder.setOrderTime(DateUtil.toHH_mm_ss(now));
            withdrOrder.setOrderStatus(orderStatus);
            withdrOrder.setPrice(price);
            withdrOrder.setWithdrPrice(withdrPrice);
            withdrOrder.setWithdrRatio(withdrRatio);
            withdrOrder.setBankCode(withdrOrderReqDTO.getBankCode());
            withdrOrder.setBankCardNo(withdrOrderReqDTO.getBankCardNo());
            withdrOrder.setBankCardType(withdrOrderReqDTO.getBankCardType());
            withdrOrder.setCheckName("NO_CHECK");
            withdrOrder.setClientIp(withdrOrderReqDTO.getClientIp());
            withdrOrder.setOutAgrNo(withdrOrderReqDTO.getAgrNo());
            withdrOrder.setSysCnl(withdrOrderReqDTO.getSysCnl());
            withdrOrder.setTradeType(withdrOrderReqDTO.getTradeType());
            withdrOrder.setAddTime(now);
            withdrOrder.setUpdateTime(now);
            iWithdrOrderService.save(withdrOrder);

            Map<String, Object> params = new HashMap<>();
            log.info("|提现申请|调用mppay系统提现|订单编号:{}|", orderNo);
            params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_WITHDR_APPLY.String());
            params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
            params.put("orderNo", orderNo);
            params.put("orderDate", DateUtil.toyyyy_MM_dd(now));
            params.put("orderTime", DateUtil.toHH_mm_ss(now));
            params.put("agrNo", withdrOrderReqDTO.getAgrNo());
            params.put("price",withdrOrderReqDTO.getPrice());
            params.put("withdrPrice", withdrOrder.getWithdrPrice());
            params.put("withdrRatio", withdrOrder.getWithdrRatio());
            params.put("userId", userId);
            params.put("bankCode", withdrOrderReqDTO.getBankCode());
            params.put("checkName", "NO_CHECK");
            params.put("bankCardNo", withdrOrderReqDTO.getBankCardNo());
            params.put("clientIp", withdrOrderReqDTO.getClientIp());
            params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
            params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());
            params.put("tradeType", withdrOrderReqDTO.getTradeType());
            params.put("bankCardType", withdrOrderReqDTO.getBankCardType());
            params.put("sysCnl", withdrOrderReqDTO.getSysCnl());

            JSONObject unbind = null;
            String exCode = null;
            String exMsg = null;
            try {
                unbind = requestResult(params);
            } catch (ShopException se) {
                exCode = se.getCode();
                exMsg = se.getMsg();
                log.error("|提现申请|调用mppay失败,code:{},msg:{}", exCode, exMsg);
            } catch (Exception e) {
                log.error("|提现申请|调用mppay异常:{}", e.getMessage());
            }
            if (unbind == null) {
                // 提现订单状态修改为:提现异常
                WithdrOrder errOrder = iWithdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("order_no", orderNo));
                errOrder.setOrderStatus(WithdrOrderStatusEnum.ERROR.getId());
                iWithdrOrderService.updateById(errOrder);
                if (StringUtils.isNotBlank(exCode) && StringUtils.isNotBlank(exMsg)) {
                    return ResponseUtil.fail(exCode, exMsg);
                }
                log.error("|提现申请|提现异常|订单号:{}, 设置订单状态:{}", orderNo, WithdrOrderStatusEnum.ERROR.getId());
                return ResponseUtil.fail(51001);
            }

            // 更新银行及银行卡信息
            String bindBankCode = unbind.getString("bankCode");
            String bindBankCardNo = unbind.getString("bankCardNo");
            String bindBankCardName = unbind.getString("bankCardName");
            if (StringUtils.isNotBlank(bindBankCode)) {
                withdrOrder.setBankCode(bindBankCode);
            }
            if (StringUtils.isNotBlank(bindBankCardNo)) {
                withdrOrder.setBankCardNo(bindBankCardNo);
            }
            withdrOrder.setBankCardName(bindBankCardName);
            iWithdrOrderService.updateById(withdrOrder);

            BeanUtils.copyProperties(withdrOrder, withdrOrderDTO);
        } catch (Exception e) {
            log.error("提现异常:{}", e);
            return ResponseUtil.fail(51001);
        }
        return ResponseUtil.ok("提现申请成功，请耐心等待审核结果！", withdrOrderDTO);
    }

    @PostMapping("/bank/bind/check")
    public Object bindCheck(@LoginUser Long userId, @RequestBody WithdrOrderReqDTO withdrOrderReqDTO) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_QUERY_WITHDR_BANK.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("userId", userId);
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrderReqDTO.getPlatform());
        params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        // 绑定的银行卡（支、微信）列表
        JSONObject bankList = requestResult(params);

        int check = 0;// 0：未绑定、1：已绑定
        if (bankList != null) {
            // bindType
            JSONArray cardBindList = bankList.getJSONArray("cardBindList");
            for (int i = 0; i < cardBindList.size(); i++) {
                JSONObject jsonObject = cardBindList.getJSONObject(i);
                if (StringUtils.isNotBlank(jsonObject.getString("bankCardNo"))) {
                    check = 1;
                    break;
                }
            }
        }
        return ResponseUtil.ok(MapUtil.of("check", check));
    }

    /**
     * 提现记录
     *
     * @param userId
     * @param reqMsg
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginUser Long userId, @RequestBody JSONObject reqMsg) {
        log.info("|提现记录|用户id:{}, 请求报文:{}", userId, reqMsg);
        Long page = reqMsg.getLong("page");
        Long limit = reqMsg.getLong("limit");
        IPage<WithdrOrder> iPage = new Page<>(page, limit);
        QueryWrapper<WithdrOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("add_time");
        queryWrapper.eq("user_id", userId);
        queryWrapper.in("order_status","A","W","S","F","R","TS");

        IPage<WithdrOrder> pageResult = iWithdrOrderService.page(iPage, queryWrapper);
        long total = pageResult.getTotal();
        List<WithdrOrderDTO> orderDTOList = new ArrayList<>();
        List<WithdrOrder> records = pageResult.getRecords();
        if (records != null && records.size() > 0) {
            // 银行信息完善
            List<String> bankCodes = records.stream().map(WithdrOrder::getBankCode).distinct().collect(toList());
            List<Dictionary> banks = iDictionaryService.list(new QueryWrapper<Dictionary>()
                    .eq("category", "bankCode").in("str_val", bankCodes));
            for (int i = 0; i < records.size(); i++) {
                WithdrOrder withdrOrder = records.get(i);
                WithdrOrderDTO withdrOrderDTO = new WithdrOrderDTO();
                BeanUtils.copyProperties(withdrOrder, withdrOrderDTO);
                // 银行信息处理
                String bankCode = withdrOrderDTO.getBankCode();
                withdrOrderDTO.setBankName("");
                if(StringUtils.isNotBlank(bankCode)){
                    if(banks != null && banks.size() > 0){
                        banks.forEach(bank -> {
                            if(bankCode.equals(bank.getStrVal())){
                                withdrOrderDTO.setBankName(bank.getName());
                            }
                        });
                    }
                }
                orderDTOList.add(withdrOrderDTO);
            }
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("total", total);
        dataMap.put("items", orderDTOList);
        return ResponseUtil.ok(dataMap);
    }

    private JSONObject requestResult(Map<String, Object> params) throws Exception {

        log.info("请求pay参数：{}", JacksonUtil.objTojson(params));
        String plain = Sign.getPlain(params);
        plain += "&key=" + authConfig.getMercPrivateKey();
        log.info(plain);
        String sign = Sign.sign(plain);
        log.info(sign);
        log.info("key:{}", authConfig.getMercPrivateKey());
        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
        String s = HttpUtil.sendPostJson(authConfig.getPayUrl(), params, headers);
        log.info("请求pay 结果：{}", s);
        if (StringUtils.isBlank(s)) {
            throw new ShopException(10502);
        }

        JSONObject json = JSONObject.parseObject(s);
        if (!ReqResEnum.C_10000.String().equalsIgnoreCase(json.getString(ReqResEnum.CODE.String()))) {
            log.error("请求接口失败,params[{}],response[{}]", JacksonUtil.objTojson(params), json);
            String code = json.get("code").toString();
            String msg = json.get("msg").toString();
            throw new ShopException(code, msg);
        }
        return json.getJSONObject(ReqResEnum.DATA.String());
    }

}
