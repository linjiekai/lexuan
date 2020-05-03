package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.constants.MQMessageStatusEnum;
import com.zhuanbo.core.entity.MqMessage;
import com.zhuanbo.service.mapper.MqMessageMapper;
import com.zhuanbo.service.service.IMqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * MQ处理异常消息 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-10
 */
@Service
@Slf4j
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage> implements IMqMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void simpleSave(String exchange, String routingKey, String message, Map<String, Object> header) {
        MqMessage mqMessage = new MqMessage();
        mqMessage.setExchange(exchange);
        mqMessage.setRoutingKey(routingKey);
        mqMessage.setMessage(message);
        mqMessage.setStatus(MQMessageStatusEnum.STATUS_0.value());
        mqMessage.setUuid(UUID.randomUUID().toString());
        if (header != null && header.size() > 0) {
            mqMessage.setHeader(JSON.toJSONString(header));
        }
        this.save(mqMessage);
    }

    @Override
    public void tryOrStore(Message message) throws Exception {
        simpleSave(message.getMessageProperties().getReceivedExchange(),  message.getMessageProperties().getReceivedRoutingKey(),
                new String(message.getBody(), "UTF-8"),  message.getMessageProperties().getHeaders());
    }

    @Override
    public int updateStatusByUUID(Integer status, String uuid) {
        return baseMapper.updateStatusByUUID(status, uuid);
    }

    @Override
    public void n2Save(Integer times, String exchange, String message, String routingKey, String uuid) {
        MqMessage mqMessage = new MqMessage();
        mqMessage.setTimes(times);
        mqMessage.setUuid(uuid);
        mqMessage.setStatus(MQMessageStatusEnum.STATUS_0.value());
        mqMessage.setExchange(exchange);
        mqMessage.setMessage(message);
        mqMessage.setRoutingKey(routingKey);
        save(mqMessage);
    }
}
