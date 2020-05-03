package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.client.server.client.PayClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.dto.PayCardBindDTO;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.service.service.IPayCardBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @title: PayCardBindServiceImpl
 * @description: 卡绑定信息
 * @date 2020/4/15 15:57
 */
@Service
@Slf4j
public class PayCardBindServiceImpl implements IPayCardBindService {

    @Resource
    private PayClient payClient;
    @Resource
    private AuthConfig authConfig;

    /**
     * 根据协议号查询绑定信息
     *
     * @param cardBind
     * @return
     */
    @Override
    public PayCardBindDTO getCardBindByAgrNo(PayCardBindDTO cardBind) {
        cardBind.setMercId(authConfig.getMercId());
        ResponseDTO responseDTO = payClient.getCardBindByAgrNo(cardBind);
        if (responseDTO == null) {
            log.error("|获取卡绑定信息|异常|");
            throw new ShopException("获取卡绑定信息异常");
        }
        String code = responseDTO.getCode();
        if (!Constants.SUCCESS_CODE.equals(code)) {
            log.error("|获取卡绑定信息|失败,code:{}, message:{}|", code, responseDTO.getMsg());
            throw new ShopException("获取卡绑定信息失败");
        }
        Object cardBindObj = responseDTO.getData();
        String cardBindStr = JSONObject.toJSONString(cardBindObj);
        JSONObject cardBindJson = JSONObject.parseObject(cardBindStr);
        PayCardBindDTO payCardBindDTO = JSONObject.toJavaObject(cardBindJson, PayCardBindDTO.class);
        return payCardBindDTO;
    }
}
