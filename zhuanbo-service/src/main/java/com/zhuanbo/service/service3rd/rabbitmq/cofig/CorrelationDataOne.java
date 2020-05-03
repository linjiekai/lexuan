package com.zhuanbo.service.service3rd.rabbitmq.cofig;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;

@Data
@AllArgsConstructor
public class CorrelationDataOne extends CorrelationData {

    private String id;
    private String exchange;
    private String routingKey;
    private Message message;
}
