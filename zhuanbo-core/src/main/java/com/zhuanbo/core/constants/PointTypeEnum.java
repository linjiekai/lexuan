package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum PointTypeEnum {

    /*** 1:充值 */
    DEPOSIT(1, "充值"),
    /*** 2:支付扣减 */
    PAY(2, "支付扣减"),
    ;

    private int id;
    private String name;

}

