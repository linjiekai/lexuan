package com.zhuanbo.service.service3rd.rabbitmq;

public interface IRabbitMQSenderService {

    /**
     * 异常推送
     * @param action
     * @param data
     */
    void send(String action, Object data);
}
