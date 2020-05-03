package com.zhuanbo.core.sms.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SMSConfig {

    @Value("${storage.aliyun.accessKeyId}")
    private String accessKeyId;
    @Value("${storage.aliyun.accessKeySecret}")
    private String accessKeySecret;


    @Bean(name = "defaultAcsClient")
    public DefaultAcsClient defaultAcsClient(){
        DefaultProfile profile = DefaultProfile.getProfile("cn-shenzhen", accessKeyId, accessKeySecret);
        return new DefaultAcsClient(profile);
    }
}
