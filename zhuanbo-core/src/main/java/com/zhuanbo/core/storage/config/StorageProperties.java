package com.zhuanbo.core.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {
    private String active;
    private Local local;
    private Aliyun aliyun;

    @Data
    public static class Local {
        private String address;
        private String storagePath;

    }
    @Data
    public static class Aliyun {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String url;
        private Long expires;
        private String privateBucketName;
        private String stsDomain;
        private String roleArn;
        private String roleSessionName;
        private String privateEndpoint;
    }
}
