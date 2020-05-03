package com.zhuanbo.service.strategy;


import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderGoods;

import java.util.List;
import java.util.Map;

/**
 * 订单完成分润策略
 */
public abstract class OrderProfitStrategy {
    /**
     * 订单开始分润处理
     * @param order 订单
     * @param cid 公司账号
     * @param orderGoodsList 订单商品
     * @param params 其他参数
     * @return {notify:list(通知)}
     * @throws Exception
     */
    public abstract Map<String, Object> orderProfit (Order order, Long cid, List<OrderGoods> orderGoodsList, Map<String, Object> params) throws Exception;
}
