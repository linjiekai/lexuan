package com.zhuanbo.service.handler;

import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.vo.ResponseMsgVO;

/**
 * @author: Jiekai Lin
 * @Description(描述): 	分润的处理
 * @date: 2019/11/7 16:04
 */
public interface IProfitProcHandler {

	/**
	 * @Description(描述): 退款分润处理
	 * @auther: Jack Lin
	 * @param :[order]
	 * @return :void
	 * @date: 2019/11/7 15:59
	 */
	void refundProfitProc(Order order, ResponseMsgVO responseMsgVO) throws Exception;
}
