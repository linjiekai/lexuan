package com.zhuanbo.shop.api.scheduled;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.MQMessageStatusEnum;
import com.zhuanbo.core.entity.MqMessage;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service3rd.rabbitmq.cofig.CorrelationDataOne;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * mq异常消息重新处理
 */
@Component
@Slf4j
public class MQMessageScheduled {

	public static final String ZHUANBO_MQ_MESSAGE_RESEND_LOCK = "zhuanbo:mq:message:resend:lock";
	@Autowired
	private IMqMessageService iMqMessageService;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private RedissonDistributedLocker redissonDistributedLocker;

	// 用来标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULED_STATUS = 0;
		
	@Scheduled(cron = "${scheduled.mq-message}")
	public void execute() {
		
		if (Constants.SCHEDULER_SWITCH == 0) {
			SCHEDULED_STATUS = 0;
			return;
    	}
		boolean tryLock = redissonDistributedLocker.tryLock(ZHUANBO_MQ_MESSAGE_RESEND_LOCK, TimeUnit.SECONDS, 1, 60 * 5);
		if (!tryLock) {
			return;
		}

		SCHEDULED_STATUS = 1;
		LogUtil.SCHEDULED.info("MQ消息重发");
		LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-1);
		int page = 1;
		int limit = 500;
		IPage<MqMessage> mqMessageIPage = iMqMessageService.page(new Page<>(page, limit), new QueryWrapper<MqMessage>()
				.eq("status", MQMessageStatusEnum.STATUS_0.value()).le("add_time", localDateTime).last("and length(exchange) > 0 and length(routing_key) > 0"));
		List<MqMessage> list = mqMessageIPage.getRecords();
		MqMessage mm = new MqMessage();
		while (CollectionUtils.isNotEmpty(list)) {
			for (MqMessage mqMessage : list) {
				try {
					if (Constants.SCHEDULER_SWITCH == 0) {
						SCHEDULED_STATUS = 0;
						return;
					}
					if (mqMessage.getTimes() >= 3 ) {
						continue;
					}
					boolean update = iMqMessageService.update(mm, new UpdateWrapper<MqMessage>().set("times", mqMessage.getTimes() + 1).eq("id", mqMessage.getId()).eq("times", mqMessage.getTimes()));
					if (update) {


						MessageProperties messageProperties = new MessageProperties();
						if (StringUtils.isNotBlank(mqMessage.getHeader())) {
							Map<String, Object> map = JSON.parseObject(mqMessage.getHeader(), Map.class);
							for (String key : map.keySet()) {
								messageProperties.setHeader(key, map.get(key));
							}
						}
						Message message = new Message(mqMessage.getMessage().getBytes(), messageProperties);
						CorrelationDataOne correlationDataOne = new CorrelationDataOne(mqMessage.getUuid(),
								mqMessage.getExchange(), mqMessage.getRoutingKey(), message);
						rabbitTemplate.convertAndSend(mqMessage.getExchange(), mqMessage.getRoutingKey(), message, correlationDataOne);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			page++;
			mqMessageIPage = iMqMessageService.page(new Page<>(page, limit), new QueryWrapper<MqMessage>()
					.eq("status", MQMessageStatusEnum.STATUS_0.value()).le("add_time", localDateTime).last("and length(exchange) > 0 and length(routing_key) > 0"));
			list = mqMessageIPage.getRecords();
		}
		SCHEDULED_STATUS = 0;
		try {
			redissonDistributedLocker.unlock(ZHUANBO_MQ_MESSAGE_RESEND_LOCK);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 清理已发送的消息(1周前的)
	 */
	@Scheduled(cron = "1 1 1 0/1 * ?")
	public void clear() {
		LocalDateTime localDateTime = LocalDateTime.now().plusDays(-7);
		iMqMessageService.remove(new QueryWrapper<MqMessage>().eq("status", MQMessageStatusEnum.STATUS_1.value()).lt("add_time", localDateTime));
	}
}
