package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 调怅记录:订单类型
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum IncomeAdjustOrderTypeEnum {

    /**
     * 调怅记录:订单类型:0:订单
     */
    ORDER(0, "订单"),
    /**
     * 调怅记录:订单类型:1:充值
     */
    DEPOSIT(1, "充值"),
    ;

    private int id;
    private String name;
}
