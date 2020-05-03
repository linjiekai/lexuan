package com.zhuanbo.core.sms.impl;

import com.zhuanbo.client.server.client.CommonClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.client.server.dto.common.SmsDTO;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.PlatformType;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.shop.api.ShopApiApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApiApplication.class)
@Slf4j
public class SendSmsServiceImplTest {

    @Autowired
    CommonClient client;
    @Autowired
    private AuthConfig authConfig;

    @Test
    public void sendSms() {
        SmsDTO dto = SmsDTO.builder().build();
        dto.setMobile("13509030019");
        dto.setPlatForm(PlatformType.ZBMALL.getCode());
        dto.setTemplateId(authConfig.getSmsTemplateRegister());
        ResponseDTO responseDTO = client.sendSms(dto);
        System.out.println(JacksonUtil.objTojson(responseDTO));
    }
}