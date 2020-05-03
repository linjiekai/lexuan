package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 平台收益明细-订单类型
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PlatformIncomeOrderTypeEnum {

    /**
     * 订单类型:0:交易
     */
    TRADE(0, "交易"),
    /**
     * 订单类型:1:退款
     */
    REFUND(1, "退款"),
    ;

    private int id;
    private String name;
}
