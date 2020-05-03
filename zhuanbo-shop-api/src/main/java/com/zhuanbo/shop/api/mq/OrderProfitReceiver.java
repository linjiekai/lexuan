package com.zhuanbo.shop.api.mq;


import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.zhuanbo.core.constants.ChangeType;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.OrderTypeEnum;
import com.zhuanbo.core.dto.OrderProfitResultDTO;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.entity.UserIncomeSyn;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserIncomeSynService;

import lombok.extern.slf4j.Slf4j;


/**
 * 订单分润记录处理
 */

@Component
@Slf4j
public class OrderProfitReceiver {

    @Autowired
    private IUserIncomeSynService iUserIncomeSynService;
    
    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;
    
    @Autowired
    private IUserIncomeService iUserIncomeService;
    
    @Autowired
    private PlatformTransactionManager txManager;
    
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    
    @Autowired
    private IMqMessageService iMqMessageService;
    
    @Autowired
    private IOrderService iOrderService;
    
    @Autowired
    private IDepositOrderService iDepositOrderService;
    
	// 用来标识MQ状态 1：开启 0：关闭
   	public static Integer MQ_STATUS = 0;

    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.order-profit-result.queue}",
            durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}",
                    type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.order-profit-result.routing-key}"))
    public void deal(@Payload Message message, Channel channel) throws Exception {

    	String ms = new String(message.getBody(), "UTF-8");
    	LogUtil.SHARE_PROFIT.info("订单记录分润结果接收|{}", ms);
    	
        if (Constants.MQ_SWITCH == 0) {
            MQ_STATUS = 0;
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            return;
        }
        MQ_STATUS = 1;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        
        boolean locked = false;
        try {

            locked = redissonLocker.tryLock(Constants.ORDER_PROFIT_LOCK_KEY, TimeUnit.SECONDS, Constants.LOCK_WAIT_TIME, Constants.LOCK_LEASE_TIME);

            JSONObject result = JSON.parseObject(ms);
            if (result == null) {
                return;
            }
            JSONArray items = result.getJSONArray("items");
            
            if (items != null) {
                if (items != null && items.size() > 0) {
                    List<OrderProfitResultDTO> orderProfitResultDTOList = JSON.parseArray(items.toJSONString(), OrderProfitResultDTO.class);
                    int index = 0;
                    String orderNo = null;
                    UserIncomeSyn userIncomeSyn = null;
                    
                    orderNo = orderProfitResultDTOList.get(0).getOrderNo();
                    
                    Integer count = iUserIncomeDetailsService.count(new QueryWrapper<UserIncomeDetails>()
                			.eq("order_no", orderNo)
                			.eq("change_type", ConstantsEnum.CHANGE_TYPE_3.integerValue())
                			);
                    
                    if (count > 0) {
                    	LogUtil.SHARE_PROFIT.info("重复分润结果,不再处理{}", ms);
                    	return;
                    }
                    
                    Integer incomeType = null;
                    
                    for (OrderProfitResultDTO orderProfitResultDTO : orderProfitResultDTOList) {
                    	if (index == 0) {
                    		index++;
                    		iUserIncomeSynService.remove(new QueryWrapper<UserIncomeSyn>().eq("order_no", orderProfitResultDTO.getOrderNo()));
                    	}
                    	if (orderProfitResultDTO.getRewardType() == ConstantsEnum.REWARD_TYPE_1.integerValue()) {
                    		incomeType = ConstantsEnum.INCOME_TYPE_1.integerValue();
                    	} else if (orderProfitResultDTO.getRewardType() == ConstantsEnum.REWARD_TYPE_2.integerValue()) {
                    		incomeType = ConstantsEnum.INCOME_TYPE_2.integerValue();
                    	}
                    	
                    	if (orderProfitResultDTO.getProfitType() == 7) {
                    		incomeType = ConstantsEnum.INCOME_TYPE_3.integerValue();
                    	} else if (orderProfitResultDTO.getProfitType() == 9) {
                    		incomeType = ConstantsEnum.INCOME_TYPE_5.integerValue();
                    	} else if (orderProfitResultDTO.getProfitType() == 11) {
                    		incomeType = ConstantsEnum.INCOME_TYPE_6.integerValue();
                    	}
                    	
                    	userIncomeSyn = new UserIncomeSyn();
                    	BeanUtils.copyProperties(orderProfitResultDTO, userIncomeSyn);
                    	
                    	userIncomeSyn.setIncomeType(incomeType);
                    	iUserIncomeSynService.save(userIncomeSyn);
                	}
                    
                    List<UserIncomeSyn> listUserIncomeSyn = iUserIncomeSynService.list(new QueryWrapper<UserIncomeSyn>().select("order_no", "profit_user_id", "reward_type", "operate_type", "sum(profit_amount) profit_amount, income_type").eq("order_no", orderNo).groupBy("order_no", "profit_user_id", "reward_type", "operate_type", "income_type"));
                    
                    Order order = null;
                    DepositOrder depositOrder = null;
                    
                    String content = null;
                    
                    status = txManager.getTransaction(def);
                    for (UserIncomeSyn syn : listUserIncomeSyn) {
                    	switch (syn.getRewardType()) {
                    	case 1:
                    		content = "购买商品";
                    		
                    		if (syn.getIncomeType() == ConstantsEnum.INCOME_TYPE_6.integerValue()) {
                    			content = "购买商品运费";
                    		}
                    		
                    		order = iOrderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
                    		if (null == order) {
                    			LogUtil.SHARE_PROFIT.info("订单记录分润,订单不存在{}", orderNo);
                    			continue;
                    		}
                    		iUserIncomeService.goodsOrder(order, syn, syn.getIncomeType(), ChangeType.INCOME.getId(), content, null);
							break;

                    	case 2:
                			depositOrder = iDepositOrderService.getOne(new QueryWrapper<DepositOrder>().eq("deposit_no", orderNo));
                			if (null == depositOrder) {
                    			LogUtil.SHARE_PROFIT.info("充值订单记录分润,订单不存在{}", orderNo);
                    			continue;
                    		}
                			content = "购买" + OrderTypeEnum.parse(depositOrder.getOrderType()).getName() +"套餐";
                			if (depositOrder.getPrice().doubleValue() == 0) {
                				content = "会员升级";
                			}
                			
                			iUserIncomeService.depositOrder(depositOrder, syn, syn.getIncomeType(), ChangeType.INCOME.getId(), content, null);
							break;
						default:
							throw new ShopException("分润类型不存在" + ms);
						}
                    }
                    
                    txManager.commit(status);
                }
            }
        } catch (Exception e) {
            log.error("消息处理订单分润失败:" + ms,e);
            if (status != null) {
                txManager.rollback(status);
            }
            iMqMessageService.tryOrStore(message);
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
        	if (locked) {
                redissonLocker.unlock(Constants.ORDER_PROFIT_LOCK_KEY);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        
        MQ_STATUS = 0;
    }
}
