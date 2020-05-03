package com.zhuanbo.shop.api.mq;


import com.rabbitmq.client.Channel;
import com.zhuanbo.service.service.IMqMessageService;
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
 * 初始化同步树
 */

@Component
@Slf4j
public class GUserReceiver {

    @Autowired
    private IMqMessageService iMqMessageService;
    /*@Autowired
    private IGraphService iGraphService;*/

    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.arangodb.queue}", durable = "true"), exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.arangodb.routing-key}"))
    public void process(@Payload Message message, Channel channel) throws Exception {
        try {
            // 不再由Shop系统维护
            //String s = new String(message.getBody(), "utf-8");
            /**
             * 三种情况
             *  {type:u, data:{id:0}}
             *  {type:uf, data:{tid:0, fid:0}}
             *  {type:u_f, data:{tid:0, fid:0}}
             */
            //JSONObject json = JSON.parseObject(s);
            //iGraphService.addByJson(json);
        } catch (Exception e) {
            log.error("MQ:GUserReceiver:process:异常:{}",e);
            iMqMessageService.tryOrStore(message);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
