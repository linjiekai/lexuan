package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 平台收益类型
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PlatformIncomeTypeEnum {

    /*** 1:购买订单 */
    BUY(1, "购买订单"),
    /*** 2:充值 */
    DEPOSIT(2, "充值"),
    /*** 3:提现 */
    WITHDR(3, "提现"),
    ;

    private int id;
    private String name;
}
