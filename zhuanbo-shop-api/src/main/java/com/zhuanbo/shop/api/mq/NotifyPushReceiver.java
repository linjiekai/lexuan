package com.zhuanbo.shop.api.mq;


import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.zhuanbo.service.service.IPushService;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


/**
 * 通知消息(只发，不做任何处理)
 */

@Component
@Slf4j
public class NotifyPushReceiver {

    @Autowired
    private IPushService iPushService;

    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.notify-push.queue}", durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC),
            key = "${spring.rabbitmq.queues.notify-push.routing-key}"), concurrency = "3")
    public void notifyPush(@Payload Message message, Channel channel) throws Exception {

        try {
            String msg = new String(message.getBody(), "utf-8");
            log.info("MQ|Notify|{}", msg);
            NotifyPushMQVO notifyPushMQVO = JSON.parseObject(msg, NotifyPushMQVO.class);
            iPushService.doByAction(notifyPushMQVO);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("MQ:notifyPush:ERROR:{}", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
