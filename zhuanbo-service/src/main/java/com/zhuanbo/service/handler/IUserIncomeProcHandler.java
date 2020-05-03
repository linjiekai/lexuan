package com.zhuanbo.service.handler;

import com.zhuanbo.core.dto.OrderRefundDto;

/**
 * 用户收益利润分配处理接口
 *
 * @author chenfeihang
 */
public interface IUserIncomeProcHandler {

    /**
     * 退款订单利润分配
     *
     * @throws Exception
     */
    void orderRefundProc(OrderRefundDto orderRefundDto) throws Exception;

}
