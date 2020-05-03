package com.zhuanbo.shop.api.mq;

import com.rabbitmq.client.Channel;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户收益
 */
@Component
@Slf4j
public class UserIncomeReceiver {

    final String REDIS_KEY = "TEAM_COUNT_COMPANY_1";
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IUserService iUserService;

    /**
     * 用来标识MQ状态 1：开启 0：关闭
     */
    public static Integer MQ_STATUS = 0;
    public static Integer MQ_STATUS_2 = 0;
    /**
     * mq入库开关 1：开启 0：关闭
     */
    public static Integer MQ_INSERT_SWITCH = 1;
    public static Integer MQ_INSERT_SWITCH_2 = 1;

    /**
     * 在途收益转可提现收益
     *
     * @param msg
     * @param channel
     * @param message
     * @throws Exception
     */
    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.income-change-deposit.queue}", durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.income-change-deposit.routing-key}"))
    public void incomeChangeDeposit(String msg, Channel channel, Message message) throws Exception {
        String bodyMsg = new String(message.getBody(), "utf-8");
        log.info("MQ=收益=在途收益转可提现收益=接口消息：{}", bodyMsg);
        boolean locked = false;
        try {
            if (StringUtils.isBlank(bodyMsg)) {
                return;
            }
            if (Constants.MQ_SWITCH.intValue() == 0) {
                MQ_STATUS = 0;
                log.info("MQ=收益=在途收益转可提现收益=开关关闭：{}", bodyMsg);
                //入库
                if (MQ_INSERT_SWITCH.intValue() == 1) {
                    iMqMessageService.tryOrStore(message);
                    log.info("MQ=收益=在途收益转可提现收益=入库开关打开，消息入库：{}", bodyMsg);
                    return;
                }
                return;
            }
            MQ_STATUS = 1;
            locked = redissonLocker.tryLock(Constants.INCOME_CHANGE_DEPOSIT_LOCK_KEY, TimeUnit.SECONDS, Constants.LOCK_WAIT_TIME, Constants.LOCK_LEASE_TIME);
            if (!locked) {
                throw new ShopException("获取分布式失败lockFlag=" + locked + ",bodyMsg=" + bodyMsg);
            }

            Long userId = Long.valueOf(bodyMsg);
            User user = iUserService.getById(userId);
            if (user == null) {
                log.error("MQ=收益=在途收益转可提现收益,ERROR:用户{}不存在", userId);
                return;
            }

            // 在途收益转可提现收益
            log.info("MQ=收益=在途收益转可提现收益,INFO:开始转换,用户id:{}", userId);
            iUserIncomeService.income2Deposit(userId, null);

        } catch (Exception e) {
            log.error("MQ=收益=在途收益转可提现收益=ERROR, 用户Id:{}, 错误信息:{}", bodyMsg, e);
            iMqMessageService.tryOrStore(message);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            if (locked) {
                redissonLocker.unlock(Constants.INCOME_CHANGE_DEPOSIT_LOCK_KEY);
            }
            log.info("MQ=收益=在途收益转可提现收益=结束：{}", bodyMsg);
            MQ_STATUS = 0;
        }
    }

}
