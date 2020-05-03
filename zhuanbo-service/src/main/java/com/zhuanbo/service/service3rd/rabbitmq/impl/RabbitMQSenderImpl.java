package com.zhuanbo.service.service3rd.rabbitmq.impl;

import com.alibaba.fastjson.JSON;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.MapKeyEnum;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.cofig.CorrelationDataOne;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class RabbitMQSenderImpl implements IRabbitMQSenderService {

	public final static String UNIQUE_SIGN_ID = "unique_sign_id";
    /**
     * 同步支付那边的用户：添加
     */
    public final static String PAY_ADD = "pay_add";
    public final static String PAY_ADD_NEW = "pay_add_new";
    /**
     * 同步支付那边的用户：更新
     */
    public final static String PAY_UPDATE = "pay_update";
    
    /**
     * 同步支付那边的用户：添加
     */
    public final static String LIVE_USER_ADD = "live_user_add";
    public final static String LIVE_USER_ADD_NEW = "live_user_add_new";
    
    public final static String LIVE_USER_UPDATE = "live_user_update";
    
    /**
     * 同步图形
     */
    public final static String G_USER = "g_user";
    /**
     * 同步图形关系
     */
    public final static String G_USER_OF = "g_user_of";
    /**
     * 同步图形与关系
     */
    public final static String G_USER_N_OF = "g_user_n_of";
    /**订单分润通知*/
    public final static String SHOP_PROFIT_ORDER = "shop_profit_order";
    /**分润系统*/
    public final static String USER_MODIFY_PROFIT = "user_modify_profit";
    public final static String USER_MODIFY_PROFIT_NEW = "user_modify_profit_new";
    /**用户兑换码升级*/
    public final static String USER_MODIFY_LEVEL = "user_modify_level";
    /**推送通知*/
    public final static String PUSH_NOTIFY = "push_notify";
    
    /**
     * 用户收益-同步团队人数
     */
    public final static String USER_INCOME_TEAM_SIZE = "user_income_team_size";
    /**
     * [在途收益]转化为[可提收益]
     */
    public final static String INCOME_CHANGE_DEPOSIT = "income_change_deposit";
    /**
     * 本地订单
     */
    public final static String LOCAL_ORDER = "local_order";
    /** 赠品回退*/
    public final static String ROLL_BACK_DEDUCT_STOCK = "rollbackDeductStock";

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private QueueConfig queueConfig;

    @Override
    public void send(String action, Object data) {
        switch (action) {
            case PAY_ADD :
                sendToPayAdd(data);
                break;
            case PAY_UPDATE :
                sendToPayUpdate(data);
                break;
            case G_USER :
                sendToGUser(data);
                break;
            case G_USER_OF :
                sendToGUserOf(data);
                break;
            case G_USER_N_OF :
                sendToGUserNOf(data);
                break;
            case LIVE_USER_ADD:
            	sendToLiveAddOrUpdate(data, "ADD");
                break;
            case LIVE_USER_ADD_NEW:
                sendToLiveAddOrUpdateNew(data, "ADD");
                break;
            case LIVE_USER_UPDATE:
            	sendToLiveAddOrUpdate(data, "UPDATE");
                break;
            case SHOP_PROFIT_ORDER:
                shopProfitOrder(data, null);
                break;
            case USER_MODIFY_PROFIT:
                userModifyProfit(data, null);
                break;
            case USER_MODIFY_PROFIT_NEW:
                userModifyProfitNew(data, null);
                break;
            case USER_MODIFY_LEVEL:
                userModifyLevel(data, null);
                break;
            case PUSH_NOTIFY:
                pushNotify(data, null);
                break;
            case USER_INCOME_TEAM_SIZE :
                sendUserIncomeTeamSize(data);
                break;
            case INCOME_CHANGE_DEPOSIT :
                sendIncomeChangeDeposit(data);
                break;
            case LOCAL_ORDER :
                localOrder(data);
                break;
            case ROLL_BACK_DEDUCT_STOCK:
                rollbackDeductStock(data);
                break;
            case PAY_ADD_NEW :
                sendToPayAddNew(data);
                break;
            default:
                throw new ShopException("异步方法无效");
        }
    }

    private void rollbackDeductStock(Object data) {
        try {
            String msg = JSON.toJSONString(data);
            MessageProperties messageProperties = new MessageProperties();
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);

            CorrelationDataOne correlationDataOne = new CorrelationDataOne(UUID.randomUUID().toString(),
                    queueConfig.getExchange(), queueConfig.getQueues().getRollbackstock().getRoutingKey(), message);

            rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getRollbackstock().getRoutingKey(), message, correlationDataOne);
        } catch (Exception e) {
            log.error("推送失败|rollbackDeductStock|{}", e);
        }
    }

    private void localOrder(Object data) {
        try {
            String msg = JSON.toJSONString(data);
            MessageProperties messageProperties = new MessageProperties();
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);

            CorrelationDataOne correlationDataOne = new CorrelationDataOne(UUID.randomUUID().toString(),
                    queueConfig.getExchange(), queueConfig.getQueues().getOrder().getRoutingKey(), message);

            rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getOrder().getRoutingKey(), message, correlationDataOne);
        } catch (Exception e) {
            log.error("消息推送失败|localOrder|{}", e);
        }
    }

    private void pushNotify(Object data, Object o) {

        try {
            NotifyPushMQVO notifyPushMQVO = (NotifyPushMQVO) data;
            String msg = JSON.toJSONString(notifyPushMQVO);
            MessageProperties messageProperties = new MessageProperties();
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);

            CorrelationDataOne correlationDataOne = new CorrelationDataOne(UUID.randomUUID().toString(),
                    queueConfig.getExchange(), queueConfig.getQueues().getNotifyPush().getRoutingKey(), message);

            rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getNotifyPush().getRoutingKey(), message, correlationDataOne);
        } catch (Exception e) {
            log.error("推送消息队列失败:{},{}", data, e);
        }
    }

    private void userModifyLevel(Object data, Object o) {

        Map<String, Object> originData = (Map<String, Object>) data;
        String msg = JSON.toJSONString(data);
        MessageProperties messageProperties = new MessageProperties();
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(String.valueOf(originData.get("uuid")),
                queueConfig.getExchange(), queueConfig.getQueues().getUserModifyLevel().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUserModifyLevel().getRoutingKey(), message, correlationDataOne);
    }

    /**
     * data是个user对象
     * @param data
     */
    private void sendToPayAdd(Object data) {
        User user = (User) data;
        Map<String, Object> mqMsg = MapUtil.of("mercId", authConfig.getMercId(), "userId", user.getId(), "mobile", user.getMobile(),
                "email", user.getEmail(), "type", "ADD", "nickname", user.getNickname());

        String uuid = UUID.randomUUID().toString();
        String msg = JSON.toJSONString(mqMsg);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), message, correlationDataOne);
    }

    private void sendToPayAddNew(Object data) {

        Map<String, Object> mqMsg = (Map<String, Object>) data;
        String msg = JSON.toJSONString(mqMsg);
        String uuid = mqMsg.get(MapKeyEnum.UUID.value()).toString();

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), message, correlationDataOne);
    }

    /**
     * data是个user对象
     * @param data
     */
    private void sendToPayUpdate(Object data) {
        User user = (User) data;
        Map<String, Object> mqMsg = MapUtil.of("mercId", authConfig.getMercId(), "userId", user.getId(), "mobile", user.getMobile(),
                "email", user.getEmail(), "type", "UPDATE", "nickname", user.getNickname());

        String msg = JSON.toJSONString(mqMsg);
        String uuid = UUID.randomUUID().toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), message, correlationDataOne);
    }

    /**
     * data 是 {type:u, data:{id:0}}
     * @param data
     */
    private void sendToGUser(Object data) {
        /*User user = (User) data;
        Map<String, Object> of = MapUtil.of("type", GraphServiceImpl.ACTION_U, "data", MapUtil.of("id", user.getId()));

        String msg = JSON.toJSONString(of);
        String uuid = UUID.randomUUID().toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = new Message(msg.getBytes(), messageProperties);

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getArangodb().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getArangodb().getRoutingKey(), message, correlationDataOne);*/

    }

    /**
     * data 是 {type:uf, data:{tid:0, fid:0}}
     * @param data
     */
    private void sendToGUserOf(Object data) {


        String msg = JSON.toJSONString(data);
        String uuid = UUID.randomUUID().toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }
        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getArangodb().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getArangodb().getRoutingKey(), message, correlationDataOne);
    }

    /**
     * data 是 {type:u_f, data:{tid:0, fid:0}}
     * @param data
     */
    private void sendToGUserNOf(Object data) {

        String msg = JSON.toJSONString(data);
        String uuid = UUID.randomUUID().toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getArangodb().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getArangodb().getRoutingKey(), message, correlationDataOne);
    }
    
    private void sendToLiveAddOrUpdate(Object data, String type) {
        User user = (User) data;
        Map<String, Object> mqMsg = MapUtil.of("mercId", authConfig.getMercId(), "userId", user.getId(), "mobile", user.getMobile(),
                "type", type, "nickname", user.getNickname(), "headImgUrl", user.getHeadImgUrl());

        String msg = JSON.toJSONString(mqMsg);
        String uuid = UUID.randomUUID().toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getLiveUser().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getLiveUser().getRoutingKey(), message, correlationDataOne);
    }

    private void sendToLiveAddOrUpdateNew(Object data, String type) {

        Map<String, Object> mqMsg = (Map<String, Object>) data;
        String uuid = mqMsg.get(MapKeyEnum.UUID.value()).toString();

        String msg = JSON.toJSONString(mqMsg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getLiveUser().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getLiveUser().getRoutingKey(), message, correlationDataOne);
    }

    private void shopProfitOrder(Object data, String type) {
        try {

            Map<String, Object> dataOrigin = (Map<String, Object>) data;
            String msg = JSON.toJSONString(data);

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setMessageId(dataOrigin.get("uuid").toString());
            Message message = new Message(msg.getBytes("UTF-8"), messageProperties);

            CorrelationDataOne correlationDataOne = new CorrelationDataOne(dataOrigin.get("uuid").toString(),
                    queueConfig.getExchange(), queueConfig.getQueues().getUser().getRoutingKey(), message);

            rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getOrderProfit().getRoutingKey(), message, correlationDataOne);
        } catch (Exception e) {
            log.error("发送订单队列失败:{}", e);
        }
    }

    private void userModifyProfit(Object data, String type) {
        User user = (User) data;
        Map<String, Object> mqMsg = new HashMap<>();
        mqMsg.put("userId", user.getId());
        mqMsg.put("inviteUpUserId", user.getInviteUpUserId());
        String msg = JSON.toJSONString(mqMsg);

        String uuid = UUID.randomUUID().toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getUserModifyProfit().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUserModifyProfit().getRoutingKey(), message, correlationDataOne);
    }

    private void userModifyProfitNew(Object data, String type) {

        Map<String, Object> mqMsg = (Map<String, Object>) data;
        String msg = JSON.toJSONString(mqMsg);

        String uuid = mqMsg.get(MapKeyEnum.UUID.value()).toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(uuid);
        Message message = null;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("编码错误");
        }

        CorrelationDataOne correlationDataOne = new CorrelationDataOne(uuid,
                queueConfig.getExchange(), queueConfig.getQueues().getUserModifyProfit().getRoutingKey(), message);

        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUserModifyProfit().getRoutingKey(), message, correlationDataOne);
    }
    
    /**
     * data 是 [当前用户id, 原来的上级id]
     * @param data
     */
    private void sendUserIncomeTeamSize(Object data){
        Message message = makeMessage(data);
        CorrelationDataOne correlationDataOne = makeCorrelationDataOne(message, queueConfig.getExchange(), queueConfig.getQueues().getUserIncomeTotalTeam().getRoutingKey());
        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getUserIncomeTotalTeam().getRoutingKey(), message, correlationDataOne);
    }

    /**
     * data 是 userId
     * @param data
     */
    private void sendIncomeChangeDeposit(Object data){
        Message message = makeMessage(data);
        CorrelationDataOne correlationDataOne = makeCorrelationDataOne(message, queueConfig.getExchange(), queueConfig.getQueues().getIncomeChangeDeposit().getRoutingKey());
        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getIncomeChangeDeposit().getRoutingKey(), message, correlationDataOne);
    }

    private Message makeMessage(Object data) {

        String msg = JSON.toJSONString(data);

        String strId = UUID.randomUUID().toString() + new Random().nextInt(10000000);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(UNIQUE_SIGN_ID, strId);

        Message message;
        try {
            message = new Message(msg.getBytes("UTF-8"), messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new ShopException("mq转码异常");
        }
        return message;
    }
    
    private CorrelationDataOne makeCorrelationDataOne(Message message, String exchange, String routingKey) {
        CorrelationDataOne correlationDataOne = new CorrelationDataOne(message.getMessageProperties().getHeaders().get(UNIQUE_SIGN_ID).toString(),
                exchange, routingKey, message);
        return correlationDataOne;
    }
}
