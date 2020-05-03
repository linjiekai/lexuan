package com.zhuanbo.service.service.impl;

import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.PayInterfaceEnum;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.service.IBankService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: BankServiceImpl
 * @projectName mpmall.api
 * @description: 银行信息
 * @date 2019/10/24 15:04
 */
@Slf4j
@Service
public class BankServiceImpl implements IBankService {

    @Autowired
    AuthConfig authConfig;

    /**
     * 根据银行code获取银行信息
     *
     * @param bankCodes
     * @return
     */
    @Override
    public List<Map<String, Object>> listByBankCodes(List<String> bankCodes) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        map.put(ReqResEnum.PLATFORM.String(), ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        map.put("requestId", System.currentTimeMillis());
        map.put("sysCnl", "WEB");
        map.put("timestamp", DateUtil.getSecondTimestamp(System.currentTimeMillis()));
        map.put("bankCodes", bankCodes);
        String plain = Sign.getPlain(map);
        plain += "&key=" + authConfig.getMercPrivateKey();
        String sign = Sign.sign(plain);
        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
        String payIp = authConfig.getPayUrlIp();
        String url = payIp + PayInterfaceEnum.BANK_LIST_BY_BANKCODE.getId();

        log.info("|银行信息列表|调用MPPAY接口|请求 url:{}, header: {},参数:{}|", url, JacksonUtil.objTojson(headers), plain);
        String respMsg = HttpUtil.sendPostJson(url, map, headers);
        log.info("|银行信息列表|调用MPPAY接口|结果：{}|", respMsg);

        List<Map<String, Object>> dataMap = new ArrayList<>();
        if (StringUtils.isNotBlank(respMsg)) {
            Map<String, Object> respMap = JacksonUtil.jsonToMap(respMsg);
            String code = (String) respMap.get("code");
            if (Constants.SUCCESS_CODE.equalsIgnoreCase(code)) {
                dataMap = (List<Map<String, Object>>) respMap.get(Constants.DATA);
            }
        }
        return dataMap;
    }
}
