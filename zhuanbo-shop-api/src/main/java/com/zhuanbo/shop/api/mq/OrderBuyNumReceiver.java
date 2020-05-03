package com.zhuanbo.shop.api.mq;


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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.OrderTypeEnum;
import com.zhuanbo.core.constants.PlatformIncomeDetailsStatusEnum;
import com.zhuanbo.core.constants.PlatformIncomeOperateTypeEnum;
import com.zhuanbo.core.constants.PlatformIncomeOrderTypeEnum;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.OrderTransDetails;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IOrderTransDetailsService;
import com.zhuanbo.service.service.IPlatformIncomeDetailsService;
import com.zhuanbo.service.service.IUserService;

import lombok.extern.slf4j.Slf4j;


/**
 * 订单分润记录处理
 */

@Component
@Slf4j
public class OrderBuyNumReceiver {

    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IDepositOrderService iDepositOrderService;
	@Autowired
	private IOrderTransDetailsService iOrderTransDetailsService;
	@Autowired
	private IPlatformIncomeDetailsService iPlatformIncomeDetailsService;
	@Autowired
    private IMqMessageService iMqMessageService;
    
	// 用来标识MQ状态 1：开启 0：关闭
   	public static Integer MQ_STATUS = 0;

    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.order-buynum.queue}",
            durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}",
                    type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.order-buynum.routing-key}"))
    public void deal(@Payload Message message, Channel channel) throws Exception {

    	String ms = new String(message.getBody(), "UTF-8");
    	LogUtil.SHARE_PROFIT.info("订单购买数量结果队列|{}", ms);
    	
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

            JSONObject result = JSON.parseObject(ms);
            if (result == null) {
            	LogUtil.SHARE_PROFIT.error("订单购买数量结果队列|{}", ms);
                return;
            }
            
            DepositOrder depositOrder = iDepositOrderService.getOne(new QueryWrapper<DepositOrder>().eq("deposit_no", result.get("orderNo")));
            if (depositOrder == null) {
            	LogUtil.SHARE_PROFIT.error("订单购买数量充值订单不存在|{}", ms);
                return;
            }
            
            User user = iUserService.getById(depositOrder.getUserId());
            
            if (user == null) {
            	LogUtil.SHARE_PROFIT.error("订单购买数量用户不存在|{}", ms);
                return;
            }
            
            if (null != iOrderTransDetailsService.getOne(new QueryWrapper<OrderTransDetails>().eq("order_no", depositOrder.getDepositNo()))) {
            	LogUtil.SHARE_PROFIT.error("订单交易已存在，不重复处理|{}", ms);
                return;
            }
            
            status = txManager.getTransaction(def);
            
            String content = "购买" + OrderTypeEnum.parse(depositOrder.getOrderType()).getName() +"套餐";
            //登记用户交易明细
	        OrderTransDetails orderTransDetails = new OrderTransDetails();
	        orderTransDetails.setOrderNo(depositOrder.getDepositNo());
	        orderTransDetails.setUserId(user.getId());
	        orderTransDetails.setPtLevel(user.getPtLevel());
	        orderTransDetails.setTransDate(DateUtil.date10());
	        orderTransDetails.setTransTime(DateUtil.date8());
	        orderTransDetails.setPrice(depositOrder.getPrice());
	        orderTransDetails.setBuyNum(result.getInteger("buyNum"));
	        orderTransDetails.setOperateType(1);
	        orderTransDetails.setPurchType(1);
	        orderTransDetails.setContent(content);
	        iOrderTransDetailsService.save(orderTransDetails);

	        if (depositOrder.getPrice().doubleValue() > 0) {
	        	// 平台收益明细记录
		        iPlatformIncomeDetailsService.save(depositOrder.getUserId(), depositOrder.getDepositNo(), PlatformIncomeOrderTypeEnum.TRADE.getId(),
	                    "", depositOrder.getPrice(), PlatformIncomeOperateTypeEnum.ADD.getId(),
	                    1, PlatformIncomeDetailsStatusEnum.EFFECTIVE.getId(), content);
	        }
            
            txManager.commit(status);
        } catch (Exception e) {
            log.error("消息处理订单分润失败:{}",e);
            if (status != null) {
                txManager.rollback(status);
            }
            iMqMessageService.tryOrStore(message);
        } finally {
	        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	    }
        
        MQ_STATUS = 0;
    }
}
