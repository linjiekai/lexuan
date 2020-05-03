package com.zhuanbo.service.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhuanbo.client.server.client.PayClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.vo.CashResultVO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 专门作用于请求支付系统操作
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
@Slf4j
public class CashServiceImpl implements ICashService {

	@Autowired
    private AuthConfig authConfig;
    @Autowired
    private PayClient payClient;

    @Override
    public JSONObject balance(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String() ,ReqResEnum.METHOD_QUERY_BALANCE.String());
        params.put("userId", userId);
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        JSONObject send = send(params);
        if (send != null && ReqResEnum.C_10000.String().equals(send.getString(ReqResEnum.CODE.String()))) {
            JSONObject data = send.getJSONObject(ReqResEnum.DATA.String());
            return data;
        }
        return null;
    }

    @Override
    public JSONObject balanceObj(Long userId) {
        JSONObject data = new JSONObject();
        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String() ,ReqResEnum.METHOD_QUERY_BALANCE.String());
        params.put("userId", userId);
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        JSONObject send = send(params);
        if (send != null && ReqResEnum.C_10000.String().equals(send.getString(ReqResEnum.CODE.String()))) {
            data = send.getJSONObject(ReqResEnum.DATA.String());
        }
        return data;
    }

    /**
     * 用户的余额 - 批量查询
     *
     * @param userIds 用户id
     * @return
     */
    @Override
    public JSONArray balanceBatch(List<Long> userIds) {
        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String() ,ReqResEnum.METHOD_QUERY_BALANCE_BATCH.String());
        params.put("userIds", userIds.toString());
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        JSONObject send = send(params);
        if (send != null && ReqResEnum.C_10000.String().equals(send.getString(ReqResEnum.CODE.String()))) {
            JSONArray data = send.getJSONArray(ReqResEnum.DATA.String());
            return data;
        }
        return null;
    }

    @Override
    public JSONObject queryWithdrOrder(WithdrOrder withdrOrder) {
        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_QUERY_WITHDR_ORDER.String());
        params.put("orderNo", withdrOrder.getOrderNo());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrder.getPlatform());
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());

        JSONObject send = send(params);
        if (send != null && ReqResEnum.C_10000.String().equals(send.getString(ReqResEnum.CODE.String()))) {
            JSONObject data = send.getJSONObject(ReqResEnum.DATA.String());
            return data;
        }
        return null;
    }

    @Override
    public CashResultVO charge(DepositOrder depositOrder) {

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> m = new HashMap<>();
        m.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_DEPOSIT_RECHARGE.String());
        m.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        m.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        m.put("orderNo", depositOrder.getDepositNo());
        m.put("orderDate", DateUtil.toyyyy_MM_dd(now));
        m.put("orderTime", DateUtil.toHH_mm_ss(now));
        m.put("price", depositOrder.getPrice());
        m.put("sysCnl", "WEB");
        m.put("userId", depositOrder.getUserId());
        m.put("mobile", depositOrder.getMobile());
        m.put("busiType", depositOrder.getBusiType());
        m.put("tradeCode", depositOrder.getTradeCode());
        m.put("bankCode", "MPPAY");
        m.put("period", 2);
        m.put("periodUnit", "02");
        m.put("clientIp", ReqResEnum.LOCAL_IP.String());
        m.put("bankCardType", "08");
        m.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        JSONObject send = send(m);
        return jsonToVo(send);
    }

    @Override
    public CashResultVO queryOrder(String orderNo) {
        Map<String, Object> m = new HashMap<>();
        m.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_QUERY_ORDER.String());
        m.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        m.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        m.put("orderNo", orderNo);
        return jsonToVo(send(m));
    }

    @Override
    public String prePay(User user, Map<String, Object> map) {

        try {
            Map<String, Object> params = new HashMap<>();
            params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_DIRECT_PREPAY.String());
            params.put("callbackUrl","");
            params.put("notifyUrl", map.get("notifyUrl"));
            params.put(ReqResEnum.MERC_ID.String(), map.get("mercId"));
            params.put(ReqResEnum.REQUEST_ID.String(),System.currentTimeMillis());
            params.put("orderNo", map.get("orderNo"));
            params.put("orderDate", map.get("orderDate"));
            params.put("orderTime", map.get("orderTime"));
            params.put("price", map.get("price"));
            params.put("sysCnl", map.get("sysCnl"));
            params.put("userId", user.getId().toString());
            params.put("period",1);
            params.put("periodUnit","02");
            params.put("mobile",user.getMobile());
            params.put("tradeCode", map.get("tradeCode"));
            params.put("busiType", map.get("busiType"));
            params.put("clientIp", map.get("clientIp"));
            params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
            params.put(ReqResEnum.PLATFORM.String(), map.get("platform"));
            String plain = Sign.getPlain(params);
            plain += "&key=" + authConfig.getMercPrivateKey();
            String sign = Sign.sign(plain);
            Map<String, Object> headers = new HashMap<String, Object>();
            headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
            headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
            log.info("submitPayInfo:params:{}", JacksonUtil.objTojson(params));
            log.info("submitPayInfo:headers:{}",JacksonUtil.objTojson(headers) );
            String resultStr = HttpUtil.sendPostJson(authConfig.getPayUrl(), params, headers);
            log.info("submitPayInfo:response{}", resultStr);
            if (StringUtils.isBlank(resultStr)) {
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(resultStr);
            if (jsonObject != null && ReqResEnum.C_10000.String().equalsIgnoreCase(jsonObject.getString(ReqResEnum.CODE.String()))) {
                JSONObject data = jsonObject.getJSONObject(ReqResEnum.DATA.String());
                if (data != null) {
                    return data.getString("prePayNo");
                }
            }
        } catch (Exception e) {
            log.error("prePay:errro:{}", e);
        }
        return null;
    }

    /**
     * 统一请求
     * @param params
     * @return
     */
    @Override
    public JSONObject send (Map<String, Object> params) {
        try {
            params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
            params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
            String plain = Sign.getPlain(params);
            plain += "&key=" + authConfig.getMercPrivateKey();
            String sign = Sign.sign(plain);
            MDC.put("X-MPMALL-Sign-PAY",sign);

            ResponseDTO responseDTO = payClient.unified(params);
            if (responseDTO != null) {
                String s = JacksonUtil.objTojson(responseDTO);
                log.info("payClient response:{}", s);
                return JSON.parseObject(s, JSONObject.class);
            }
        } catch (Exception e) {
            log.error("payClient error：{}", e);
        }
        return null;
    }

    /**
     * 异常提现订单检查
     *
     * @param withdrOrder
     * @return
     */
    @Override
    public JSONObject withdrApplyErrorCheck(WithdrOrder withdrOrder) {
        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrder.getPlatform());
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_WITHDR_APPLY_ERROR_CHECK.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("orderNo", withdrOrder.getOrderNo());

        JSONObject send = send(params);
        if (send != null && ReqResEnum.C_10000.String().equals(send.getString(ReqResEnum.CODE.String()))) {
            JSONObject data = send.getJSONObject(ReqResEnum.DATA.String());
            return data;
        }
        return null;
    }

    /**
     * json转成vo
     * @param json
     * @return
     */
    private CashResultVO jsonToVo(JSONObject json){
        if (json == null) {
            return null;
        }
        CashResultVO cashResultVO = new CashResultVO();
        cashResultVO.setCode(json.getString(ReqResEnum.CODE.String()));
        JSONObject data = json.getJSONObject(ReqResEnum.DATA.String());
        if (data != null) {
            cashResultVO.setOrderStatus(data.getString(ReqResEnum.ORDER_STATUS.String()));
            cashResultVO.setMercId(data.getString(ReqResEnum.MERC_ID.String()));
            cashResultVO.setBankCode(data.getString("bankCode"));
            cashResultVO.setOrderNo(data.getString("orderNo"));
            cashResultVO.setPayDate(data.getString("payDate"));
            cashResultVO.setPayTime(data.getString("payTime"));
            cashResultVO.setUserId(data.getString("userId"));
            cashResultVO.setPayNo(data.getString("payNo"));
            cashResultVO.setOrderDate(data.getString("orderDate"));
            cashResultVO.setOrderTime(data.getString("orderTime"));
            cashResultVO.setPrice(data.getBigDecimal("price"));
            cashResultVO.setAppId(data.getString("appId"));
            cashResultVO.setTradeType(data.getString("tradeType"));
        }
        return cashResultVO;
    }
}
