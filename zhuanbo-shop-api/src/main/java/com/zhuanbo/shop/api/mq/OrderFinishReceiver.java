package com.zhuanbo.shop.api.mq;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.BuyTypeEnum;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * 订单完成后的处理
 */

@Component
@Slf4j
public class OrderFinishReceiver {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private AuthConfig authConfig;
    
    @Autowired
    private IMqMessageService iMqMessageService;

    // 用来标识MQ状态 1：开启 0：关闭
  	public static Integer MQ_STATUS = 0;
    public static Integer MQ_STATUS_2 = 0;
  	

    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.order.queue}", durable = "true", exclusive = "fales", autoDelete = "false"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC),
            key = "${spring.rabbitmq.queues.order.routing-key}"))
    public void process(String msg, Channel channel, Message message) throws Exception {

        LogUtil.SHARE_PROFIT.info("MQ订单接收。", msg);
        if (Constants.MQ_SWITCH == 0) {
            MQ_STATUS = 0;
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            return;
        }
        MQ_STATUS = 1;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {

            status = txManager.getTransaction(def);
            Order order = JSON.parseObject(new String(message.getBody(), "UTF-8"), Order.class);
            order = iOrderService.getOne(new QueryWrapper<Order>().eq("order_no", order.getOrderNo()));
            // 目前只处理赠送订单
            if (BuyTypeEnum.BUY_TYPE_2.value().equals(order.getBuyType())) {
                doGiftOrderStatus(order);
            }
            txManager.commit(status);
        } catch (Exception e) {
            log.error("MQ处理支付完成订单失败:{}", e);
            if (status != null) {
                txManager.rollback(status);
            }
            iMqMessageService.tryOrStore(message);
	    } finally {
	        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	    }
        MQ_STATUS = 0;
    }

    /**
     * 处理赠礼状态
     * @param order
     * @throws Exception
     */
    private void doGiftOrderStatus(Order order) throws Exception {

        LocalDateTime now = LocalDateTime.now();

        PayNotifyParamsVO payNotifyParamsVO = new PayNotifyParamsVO();
        payNotifyParamsVO.setOrderNo(order.getOrderNo());
        payNotifyParamsVO.setOrderStatus(order.getOrderStatus());
        payNotifyParamsVO.setOrderDate(DateUtil.toyyyy_MM_dd(now));
        payNotifyParamsVO.setOrderTime(DateUtil.toHH_mm_ss(now));
        payNotifyParamsVO.setPayNo("0");
        payNotifyParamsVO.setMercId(order.getMercId());
        payNotifyParamsVO.setTradeType("");
        payNotifyParamsVO.setUserId(order.getUserId().toString());
        payNotifyParamsVO.setPrice(order.getPrice());
        payNotifyParamsVO.setBankCode("");
        payNotifyParamsVO.setPayDate(payNotifyParamsVO.getOrderDate());
        payNotifyParamsVO.setPayTime(payNotifyParamsVO.getOrderTime());

        Map params = JSON.parseObject(JSON.toJSONString(payNotifyParamsVO), Map.class);
        params.put("mercId", "888000000000004");
        params.put("platform", "ZBMALL");
        params.put("sysCnl", "IOS");
        params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        String plain = Sign.getPlain(params);
        plain += "&key=" + authConfig.getMercPrivateKey();
        String sign = Sign.sign(plain);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);

        String s = HttpUtil.sendPostJson(authConfig.getLocalUrl() + "/shop/mobile/pay/notify", params, headers);
        JSONObject result = JSON.parseObject(s);
        if (!result.getString("code").equals("10000")) {
            throw new ShopException("赠礼回调失败");
        }
    }

    /**
     * 回退赠品库存
     * @param channel
     * @param message
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.rollbackstock.queue}", durable = "true", exclusive = "fales", autoDelete = "false"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC),
            key = "${spring.rabbitmq.queues.rollbackstock.routing-key}"))
    public void rollbackDeductStock(@Payload Message message, Channel channel) throws Exception {

        String msg = new String(message.getBody(), "UTF-8");

        LogUtil.SHARE_PROFIT.info("赠品回退", msg);
        if (Constants.MQ_SWITCH == 0) {
            MQ_STATUS_2 = 0;
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            return;
        }
        MQ_STATUS_2 = 1;
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            JSONObject result = iOrderService.rollbackDeductStock(jsonObject.getString("no"), jsonObject.getString("userToken"));
            if (result.getString("code").equals("10000")) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        } catch (Exception e) {
            log.error("赠品回退失败:{}", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
        MQ_STATUS_2 = 0;
    }
}
