package com.zhuanbo.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证
 */
@Component
@ConfigurationProperties(prefix = "push")
@Data
public class PushConfig {


    private String urlPush;
    private String urlStatus;
    private String urlCancel;
    private String appKey;
    private String version;
    private String secret;
    private String typeBroadcast;
    private String typeCustomizedcas;
    private String aliasTypeAlias;
    private String aliasTypeFileId;
    private Integer productionMode;
    private String mipush;
    private String miActivity;
    private String afterOpen;
    private String url;
    private String activity;

    private PConfig zbmall;// 赚播商城
    private PConfig zblmall;// 赚播直播

    @Data
    public static class PConfig{
        private String urlPush;
        private String urlStatus;
        private String urlCancel;
        private String appKey;
        private String version;
        private String secret;
        private String typeBroadcast;
        private String typeCustomizedcas;
        private String aliasTypeAlias;
        private String aliasTypeFileId;
        private Integer productionMode;
        private String mipush;
        private String miActivity;
        private String afterOpen;
        private String url;
        private String activity;
    }
}
