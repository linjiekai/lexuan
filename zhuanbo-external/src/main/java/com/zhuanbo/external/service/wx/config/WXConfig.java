package com.zhuanbo.external.service.wx.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.zhuanbo.core.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WXConfig {

    @Autowired
    private AuthConfig authConfig;

    @Bean
    public WxMaService wxMaService(){

        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(authConfig.getWxMpAppId());
        config.setSecret(authConfig.getWxMpSecret());
        config.setMsgDataFormat("JSON");

        WxMaService wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(config);
        return wxMaService;
    }
}
