package com.zhuanbo.service.service3rd.rabbitmq.cofig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.constants.MQMessageStatusEnum;
import com.zhuanbo.core.entity.MqMessage;
import com.zhuanbo.service.service.IMqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ICallBack implements  RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback{

    private final ConcurrentHashMap errorSendMessageMap = new ConcurrentHashMap<String, Integer>();

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private IMqMessageService iMqMessageService;

    @PostConstruct
    public void init() {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * messsage to broker ,if exchange not exist， b = false
     *
     * @param correlationData
     * @param b
     * @param s
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {

        CorrelationDataOne correlationDataOne = null;
        if (correlationData instanceof CorrelationDataOne) {
            correlationDataOne = (CorrelationDataOne) correlationData;
        }
        // 找不到交换机，不做消息状态更新
        String correlationDataId = correlationDataOne == null ? correlationData.getId() : correlationDataOne.getId();
        if (StringUtils.isNotBlank(correlationDataId) && errorSendMessageMap.containsKey(correlationDataId)) {
            errorSendMessageMap.remove(correlationDataId);
            return;
        }
        if (b) {// 更新已发送
            if (correlationDataOne == null) {
                return;
            }
            iMqMessageService.updateStatusByUUID(MQMessageStatusEnum.STATUS_1.value(), correlationDataOne.getId());
        } else {
            log.error("消息未找到交换机:{}");
            if (correlationDataOne == null) {
                return;
            }
            // 不存在存里来，已有的不管
            MqMessage mqMessage = iMqMessageService.getOne(new QueryWrapper<MqMessage>().select("id").eq("uuid", correlationDataOne.getId()));
            if (mqMessage == null) {
                try {
                    iMqMessageService.tryOrStore(correlationDataOne.getMessage());
                } catch (Exception e) {
                    log.error("消息未找到交换机:Exception:{}", e);
                }
            }
        }
    }

    /**
     * exchange to queue, if not bind routing key between exchange and queue, show fuck message
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        try {
            log.error("没队列与交换机绑定:{}", new String(message.getBody(), "UTF-8"));
            String messageId = message.getMessageProperties().getMessageId();
            if (StringUtils.isNotBlank(messageId)) {// confirm方法用到
                errorSendMessageMap.put(messageId, 1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
