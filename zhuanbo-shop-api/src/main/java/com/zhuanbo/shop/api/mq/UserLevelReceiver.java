package com.zhuanbo.shop.api.mq;


import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.zhuanbo.core.constants.MapKeyEnum;
import com.zhuanbo.core.dto.MqUserLevelDTO;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
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

import java.util.List;
import java.util.Map;


/**
 * 用户的等级升级
 */

@Component
@Slf4j
public class UserLevelReceiver {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;

    // 用来标识MQ状态 1：开启 0：关闭
 	//public static Integer MQ_STATUS = 0;

    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.user-modify-shop.queue}", durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC),
            key = "${spring.rabbitmq.queues.user-modify-shop.routing-key}"))
    public void deal(@Payload Message message, Channel channel) throws Exception {

        try {
            String s = new String(message.getBody(), "UTF-8");
            log.info("用户消息队列信息：{}", s);

            MqUserLevelDTO mqUserLevelDTO = JSON.parseObject(s, MqUserLevelDTO.class);
            Map<String, Object> backMap = iUserService.updateUserByMQ(mqUserLevelDTO);

            if (backMap.containsKey(MapKeyEnum.PUSH_NOTIFY_LIST.value())) {
                List<NotifyPushMQVO> notifyPushMQVOList = (List<NotifyPushMQVO>) backMap.get(MapKeyEnum.PUSH_NOTIFY_LIST.value());
                notifyPushMQVOList.forEach(x ->iRabbitMQSenderService.send(RabbitMQSenderImpl.PUSH_NOTIFY, x));
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("用户消息队列信息:{}", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
