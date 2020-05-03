package com.zhuanbo.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class QueueConfig {

    private Integer enable;
    private String exchange;
    private Queues queues;


    @Data
    public static class Queues{
        private QConfig user;
        private QConfig order;
        private QConfig level;
        private QConfig levelQuick;
        private QConfig levelToLevel;
        private QConfig notifyPush;
        private QConfig videoTranscode;
        private QConfig arangodb;
        private QConfig liveUser;
        private QConfig orderProfit;
        private QConfig userModifyShop;
        private QConfig userModifyProfit;
        private QConfig userModifyLevel;
        private QConfig userIncomeTotalTeam;
        private QConfig incomeChangeDeposit;
        private QConfig rollbackstock;
        private QConfig orderBuynum;
    }

    @Data
    public static class QConfig{
        private String queue;
        private String routingKey;
    }
}
