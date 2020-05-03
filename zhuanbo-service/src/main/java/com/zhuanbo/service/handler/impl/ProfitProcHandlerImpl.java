package com.zhuanbo.service.handler.impl;

import java.util.List;
import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.constants.ChangeType;
import com.zhuanbo.core.constants.UserIncomeStatusType;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.vo.ResponseMsgVO;
import com.zhuanbo.service.handler.IProfitProcHandler;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IOrderGoodsService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserService;

import lombok.extern.slf4j.Slf4j;

@Service("profitProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ProfitProcHandlerImpl implements IProfitProcHandler {

	@Autowired
	protected IUserService userService;
	@Autowired
	protected IUserIncomeService userIncomeService;
	@Autowired
	protected IUserIncomeDetailsService userIncomeDetailsService;
	@Autowired
	protected IUserInviteService userInviteService;
	@Autowired
	protected IOrderGoodsService orderGoodsService;
	@Autowired
	protected IDictionaryService dictionaryService;
	
	@Override
	public void refundProfitProc(Order order, ResponseMsgVO responseMsgVO) throws Exception {
		log.info("|收益退款|退款原订单信息:{}", order);
		String orderNo = order.getOrderNo();
		List<UserIncomeDetails> userIncomeDetailsList = userIncomeDetailsService.list(new QueryWrapper<UserIncomeDetails>().eq("order_no", orderNo).eq("change_type", ChangeType.INCOME.getId()));
		if (null == userIncomeDetailsList || userIncomeDetailsList.size() == 0) {
			log.info("|收益退款|原订单无收益明细,原订单号:{}", orderNo);
			return;
		}
		// 原订单收益信息多条,有的成功,有的失效.
		List<UserIncomeDetails> validIncomeDetailsList = userIncomeDetailsList.stream().filter(u -> UserIncomeStatusType.NORMAL.getId() == u.getStatus()).collect(toList());
		List<UserIncomeDetails> invalidIncomeDetailsList = userIncomeDetailsList.stream().filter(u -> UserIncomeStatusType.NORMAL.getId() != u.getStatus()).collect(toList());
		invalidIncomeDetailsList.forEach(incomeDetails -> {
			log.info("|收益退款|原订单状态无效,id:{},原订单号:{},状态:{}", incomeDetails.getId(), incomeDetails.getOrderNo(), incomeDetails.getStatus());
		});
		if (validIncomeDetailsList == null || validIncomeDetailsList.size() == 0) {
			log.info("|收益退款|无有效收益明细,原订单号:{}", orderNo);
			return;
		}
		for (UserIncomeDetails userIncomeDetails : validIncomeDetailsList) {
			userIncomeService.orderRefund(userIncomeDetails, "订单退款");
		}
		return;
	}
	
	
}
