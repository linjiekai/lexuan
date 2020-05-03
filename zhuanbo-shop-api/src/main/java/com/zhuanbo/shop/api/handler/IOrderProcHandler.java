package com.zhuanbo.shop.api.handler;


import com.zhuanbo.service.vo.PayNotifyParamsVO;

import java.util.Map;

public interface IOrderProcHandler {

	/**
	 * 订单业务处理
	 * @throws Exception
	 */
	Map<String, Object> proc(PayNotifyParamsVO vo) throws Exception;

	/**
	 * 针对proc方法后的处理
	 * @param data
	 */
	void afterProc(Map<String, Object> data);
}
