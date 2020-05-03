package com.zhuanbo.shop.api.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.UserIncomeStatusType;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
import com.zhuanbo.shop.api.handler.IOrderProcHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单查询补单
 *
 */
@Component
@Slf4j
public class OrderScheduled {

	@Autowired
	private IOrderService orderService;
	@Autowired
	private AuthConfig authConfig;
	@Autowired
	private IOrderProcHandler orderProcHandler;
	@Autowired
	private IRabbitMQSenderService iRabbitMQSenderService;
	@Autowired
	private IOrderService iOrderService;
	@Autowired
	private IUserIncomeDetailsService iUserIncomeDetailsService;
	@Autowired
    private PlatformTransactionManager txManager;
	
	// 用来标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULED_STATUS = 0;
	
	public static Integer SCHEDULED_STATUS2 = 0;
	
	public static Integer SCHEDULED_STATUS3 = 0;
	
	//每2分钟轮询一次
	@Scheduled(cron = "${scheduled.order-query}")
    public void query() {
		if (Constants.SCHEDULER_SWITCH == 0) {
			SCHEDULED_STATUS = 0;
			return;
		}
		
		SCHEDULED_STATUS = 1;
		
		try {
			LogUtil.SCHEDULED.info("补单查询开始");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -2);
			Date endTime = cal.getTime();
			cal.add(Calendar.MINUTE, -60);
			Date startTime = cal.getTime();
			
			List<Order> orderList = null;
			
			orderList = orderService.list(
					new QueryWrapper<Order>().eq("order_status", OrderStatus.WAIT_PAY.getId())
					.ge("add_time", DateUtil.dateFormat(startTime, "yyyy-MM-dd HH:mm:ss"))
					.le("add_time", DateUtil.dateFormat(endTime, "yyyy-MM-dd HH:mm:ss"))
					);
			
			Map<String, Object> params = null;
			Map<String, Object> headers = null;
			Map<String, Object> resultMap = null;
			String plain = null;
			String sign = null;
			String resultStr = null;
			PayNotifyParamsVO vo = null;
			Map<String, Object> proc = null;
			for (Order order : orderList) {
				try  {
					if (Constants.SCHEDULER_SWITCH == 0) {
						SCHEDULED_STATUS = 0;
						LogUtil.SCHEDULED.info("查询补单定时器已关闭SCHEDULER_SWITCH[{}]........", SCHEDULED_STATUS);
						return;
					}
					
					params = new HashMap<String, Object>();
					params.put("methodType", "QueryOrder");
					params.put("requestId", System.currentTimeMillis());
					params.put("mercId", authConfig.getMercId());
					params.put("orderNo", order.getOrderNo());
					params.put("X-MPMALL-SignVer", "v1");
					
					plain = Sign.getPlain(params);
					plain += "&key=" + authConfig.getMercPrivateKey();
					sign = Sign.sign(plain);
					
					headers = new HashMap<String, Object>();
					headers.put("X-MPMALL-SignVer", "v1");
					headers.put("X-MPMALL-Sign", sign);
					
					resultStr = HttpUtil.sendPostJson(authConfig.getPayUrl(), params, headers);
					
					resultMap = JSONObject.parseObject(resultStr);
					
					if (null== resultMap || null == resultMap.get("code")) {
						continue;
					}
					
					//如果返回码不成功，则continue;
					if (StringUtils.isBlank(resultMap.get("code").toString()) || !Constants.SUCCESS_CODE.equals(resultMap.get("code").toString())) {
						continue;
					}
					resultMap = (Map<String, Object>) resultMap.get(Constants.DATA);
					vo = new PayNotifyParamsVO();
					BeanUtils.populate(vo, resultMap);
					
					if (OrderStatus.SUCCESS.getId().equals(vo.getOrderStatus())) {
						proc = orderProcHandler.proc(vo);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					orderProcHandler.afterProc(proc);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
			
		SCHEDULED_STATUS = 0;
    }
	
	//超时未支付订单取消
	@Scheduled(cron = "${scheduled.order-cancel}")
    public void cancel() throws  Exception{
		if (Constants.SCHEDULER_SWITCH == 0) {
			SCHEDULED_STATUS2 = 0;
			return;
		}
		SCHEDULED_STATUS2 = 1;

		String s = DateUtil.dateFormat(new Date(), DateUtil.HOUR_PATTERN);
		LogUtil.SCHEDULED.info("定时器:: 超时未支付订单取消::开始，时间段：{}",s);
		
        // 开启事务管理
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        
		try {
			LocalDateTime expTime = LocalDateTime.now().minusMinutes(authConfig.getOrderCancelTimes());

			List<Order> list = orderService.list(new QueryWrapper<Order>()
					.eq("order_status", OrderStatus.WAIT_PAY.getId())
					.le("add_time", expTime)
					);
			LogUtil.SCHEDULED.info("定时器:: 超时未支付订单取消:: list.size：{} ",list.size());
			for (Order order : list) {
				String orderNo = order.getOrderNo();
				try {
					status = txManager.getTransaction(def);
					orderService.update(new Order(), new UpdateWrapper<Order>()
							.set("order_status", OrderStatus.CANCEL.getId())
							.eq("order_status", OrderStatus.WAIT_PAY.getId())
							.eq("order_no", orderNo)
							);
					txManager.commit(status);
					LogUtil.SCHEDULED.info("定时器:: 超时未支付订单取消::成功，订单号：{}", orderNo);
				} catch (Exception e) {
					if (null != status) {
						txManager.rollback(status);
					}
					LogUtil.SCHEDULED.info("定时器:: 超时未支付订单取消::失败，订单号：{}", orderNo);
					log.error("取消订单失败, 订单号[" + orderNo + "]" , e);
				}
			}
		} catch (Exception e1) {
			if (null != status) {
				txManager.rollback(status);
			}
			log.error("取消订单失败" , e1);
			LogUtil.SCHEDULED.info("定时器:: 超时未支付订单取消::获取列表失败");
		}
		LogUtil.SCHEDULED.info("定时器:: 超时未支付订单取消::结束，时间段：{}",s);
		SCHEDULED_STATUS2 = 0;
    }
		
	
	//超过7天的订单自动改成已完成
	@Scheduled(cron = "${scheduled.order-success}")
    public void success() {
		if (Constants.SCHEDULER_SWITCH == 0) {
			SCHEDULED_STATUS3 = 0;
			return;
		}
		
		SCHEDULED_STATUS3 = 1;
		
		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime targetTime = now.plusDays(-7);
			
			List<Order> orderList = iOrderService.list(new QueryWrapper<Order>()
					.eq("order_status", OrderStatus.WAIT_DELIVER.getId())
					.lt("update_time", DateUtil.toyyyy_MM_dd_HH_mm_ss(targetTime))// 待收货，7天
					);
			
			for (Order order : orderList) {
				iOrderService.update(new Order(), new UpdateWrapper<Order>()
						.set("order_status", OrderStatus.SUCCESS.getId())
						.eq("order_status", OrderStatus.WAIT_DELIVER.getId())
						.eq("order_no", order.getOrderNo())
						);

				// 在途收益转可提收益
				List<UserIncomeDetails> userIncomeDetailsList = iUserIncomeDetailsService.list(new QueryWrapper<UserIncomeDetails>().eq("order_no", order.getOrderNo()).eq("status", UserIncomeStatusType.NORMAL.getId()));
				List<Long> userIdList = userIncomeDetailsList.stream().map(UserIncomeDetails::getUserId).collect(Collectors.toList());
				userIdList.add(order.getUserId());
				userIdList.forEach(userId -> iRabbitMQSenderService.send(RabbitMQSenderImpl.INCOME_CHANGE_DEPOSIT, userId));

			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		SCHEDULED_STATUS3 = 0;
	}
}
