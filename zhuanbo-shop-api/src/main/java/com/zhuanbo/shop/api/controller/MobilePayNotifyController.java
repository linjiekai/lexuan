package com.zhuanbo.shop.api.controller;

import com.alibaba.fastjson.JSON;
import com.zhuanbo.core.annotation.ResponseLog;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IUpgradeDetailsService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
import com.zhuanbo.shop.api.handler.IOrderProcHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * @author:
 * @Description(描述): 支付相关
 * @date: 2019/8/8 10:28
 */
@RestController
@RequestMapping("/shop/mobile/pay")
@Slf4j
@ResponseLog
public class MobilePayNotifyController {

	@Autowired
	private IOrderProcHandler orderProcHandler;
	@Autowired
	private PlatformTransactionManager txManager;
	@Autowired
	private IUpgradeDetailsService iUpgradeDetailsService;
	@Autowired
	private IRabbitMQSenderService iRabbitMQSenderService;

	/**
	 * 支付结果后台通知(普通订单)
	 * @param payNotifyParamsVO
	 * @return
	 */
	@PostMapping("/notify")
    public Object notify(@RequestBody PayNotifyParamsVO payNotifyParamsVO) {

		log.info("支付回调：{}", JSON.toJSONString(payNotifyParamsVO));
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = txManager.getTransaction(def);
		Map<String, Object> proc = null;
		try {
			proc = orderProcHandler.proc(payNotifyParamsVO);
			txManager.commit(status);
		} catch (Exception e) {
			txManager.rollback(status);
			if (e instanceof ShopException) {
				ShopException shopException = (ShopException) e;
				log.error("code[{}], return_msg[{}]", shopException.getCode(), shopException.getMsg());
				return ResponseUtil.fail(shopException.getCode(), shopException.getMsg());
			}
			log.error("支付结果异步通知处理失败" + JSON.toJSONString(payNotifyParamsVO), e);
			return ResponseUtil.fail();
		} finally {
			orderProcHandler.afterProc(proc);
		}
        return ResponseUtil.ok();
    }

	/**
	 * 押金充值回调
	 * @param payNotifyParamsVO
	 * @return
	 */
	@PostMapping("/deposit")
	public Object deposit(@RequestBody PayNotifyParamsVO payNotifyParamsVO) {

		LogUtil.SHARE_PROFIT.info(JSON.toJSONString(payNotifyParamsVO));
		try {
			Map<String, Object> backMap = iUpgradeDetailsService.generateDetail(payNotifyParamsVO);
			if (backMap != null) {
				if (backMap.containsKey("mqData")) {
					Map<String, Object> mqData = (Map<String, Object>) backMap.get("mqData");
					LogUtil.SHARE_PROFIT.info("充值订单分润通知:" + JSON.toJSONString(mqData));
					iRabbitMQSenderService.send(RabbitMQSenderImpl.SHOP_PROFIT_ORDER, mqData);
				}
			}
			return ResponseUtil.ok();
		} catch (Exception e) {
			log.error("充值订单回调异常" + JSON.toJSONString(payNotifyParamsVO), e);
			return ResponseUtil.fail();
		}
	}
}
