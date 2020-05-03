package com.zhuanbo.core.storage.config;

import com.zhuanbo.core.storage.AliyunStorage;
import com.zhuanbo.core.storage.LocalStorage;
import com.zhuanbo.core.storage.StorageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    private final StorageProperties properties;

    public StorageAutoConfiguration(StorageProperties properties) {
        this.properties = properties;
    }

    @Bean
    public StorageService storageService() {
        StorageService storageService = new StorageService();
        String active = this.properties.getActive();
        storageService.setActive(active);
        if(active.equals("local")){
            storageService.setStorage(localStorage());
        }
        else if(active.equals("aliyun")){
            storageService.setStorage(aliyunStorage());
        }
        else{
            throw  new RuntimeException("当前存储模式 " + active + " 不支持");
        }

        return storageService;
    }

    @Bean
    @RefreshScope
    public LocalStorage localStorage() {
        LocalStorage localStorage = new LocalStorage();
        StorageProperties.Local local = this.properties.getLocal();
        localStorage.setAddress(local.getAddress());
        localStorage.setStoragePath(local.getStoragePath());
        return localStorage;
    }

    @Bean
    @RefreshScope
    public AliyunStorage aliyunStorage() {
        AliyunStorage aliyunStorage =  new AliyunStorage();
        StorageProperties.Aliyun aliyun = this.properties.getAliyun();
        aliyunStorage.setAccessKeyId(aliyun.getAccessKeyId());
        aliyunStorage.setAccessKeySecret(aliyun.getAccessKeySecret());
        aliyunStorage.setBucketName(aliyun.getBucketName());
        aliyunStorage.setEndpoint(aliyun.getEndpoint());
        aliyunStorage.setUrl(aliyun.getUrl());
        aliyunStorage.setExpires(aliyun.getExpires());
        aliyunStorage.setPrivateBucketName(aliyun.getPrivateBucketName());
        aliyunStorage.setStsDomain(aliyun.getStsDomain());
        aliyunStorage.setRoleArn(aliyun.getRoleArn());
        aliyunStorage.setRoleSessionName(aliyun.getRoleSessionName());
        aliyunStorage.setPrivateEndpoint(aliyun.getPrivateEndpoint());
        return aliyunStorage;
    }
}
