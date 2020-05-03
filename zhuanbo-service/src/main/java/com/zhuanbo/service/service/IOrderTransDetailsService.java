package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderTransDetails;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户交易明细表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-11-05
 */
public interface IOrderTransDetailsService extends IService<OrderTransDetails> {
    /**
     * 转
     * @param orderTransDetailsList
     * @return
     */
    Map<String, Object> toMap(Long uid, List<OrderTransDetails> orderTransDetailsList);

    /**
     * 获取账期信息
     * @param userId
     * @return {currentPeriod,currentPeriodCount}
     * @throws ParseException
     */
    Map<String, Object> getCurrnetPeriodMsg(Long userId) throws ParseException;

    /**
     * 退款
     *
     * @param order
     */
    void orderRefund(Order order, String orderRefundNo);
}
