package com.zhuanbo.service.strategy;


import java.util.Map;

/**
 * 快速充值升级策略
 */
public abstract class QuickUpGradeStrategy {
    /**
     * 充值升级处理
     * @param uid
     * @param busiType 充值类型
     * @param orderNo 订单编号
     * @return
     * @throws Exception
     */
    public abstract Map<String, Object> quickUpGrade (Long uid, String busiType, String orderNo);
}
