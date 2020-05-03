package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户交易明细表:订单类型
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum TransDetailsOrderTypeEnum {

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
