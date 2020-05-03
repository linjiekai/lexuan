package com.zhuanbo.external.service.key.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.key.IKeyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KeyService implements IKeyService {

    private final String METHOD_TYPE_GET_APP_ID = "GetAppId";
    private final String X_MP_SIGNVER_V_1 = "v1";

    public final static String BACK_APP_ID = "appId";
    public final static String BACK_SECRECT = "secrect";
    public final static String PRIVATE_KEY = "privateKey";
    public final static String PUBLIC_KEY = "publicKey";

    @Autowired
    private AuthConfig authConfig;

    @Override
    public Map<String, Object> param(AppIdKeyDTO appIdKeyDTO) {

        try {
        	return result(appIdKeyDTO);
        } catch (Exception e) {
            log.error("KeyService.param失败：{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 统一处理
     * @param object
     * @param method
     * @param platform
     * @return
     * @throws Exception
     */
    private Map<String, Object> result(AppIdKeyDTO appIdKeyDTO) throws Exception {
        // body
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("methodType", METHOD_TYPE_GET_APP_ID);
        params.put("mercId", authConfig.getMercId());
        params.put("platform", appIdKeyDTO.getPlatform());
        params.put("requestId",System.currentTimeMillis());
        params.put("tradeType", appIdKeyDTO.getTradeType());
        params.put("bankCode", appIdKeyDTO.getBankCode());
        params.put("sysCnl", appIdKeyDTO.getSysCnl());
        params.put("operType", appIdKeyDTO.getOperType());
        // header
        String sign = Sign.sign(params, authConfig.getMercPrivateKey());
        Map<String, Object> headers = new HashMap<>();
        headers.put("X-MPMALL-SignVer", X_MP_SIGNVER_V_1);
        headers.put("X-MPMALL-Sign", sign);
        String result = HttpUtil.sendPostJson(authConfig.getPayUrl(), params, headers);
        log.info("内部APPID请求结果：{}", result);

        if (StringUtils.isBlank(result)) {
            return Maps.newHashMap();
        }
        Map<String, Object> resultMap = (Map) JSONObject.parse(result);
        
        if (Integer.parseInt(resultMap.get("code").toString()) == 10000) {
        	return (Map<String, Object>) resultMap.get("data");
        }
        
        return Maps.newHashMap();
    }
}
