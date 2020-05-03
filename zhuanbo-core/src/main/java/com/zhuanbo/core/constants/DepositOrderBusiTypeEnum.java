package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 * @title: DepositOrderBusiTypeEnum
 * @description: TODO
 * @date 2020/4/23 14:40
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DepositOrderBusiTypeEnum {

    /*** 06:会员套餐 */
    MEMBERSHIP_PACKAGE("06", "会员套餐"),
    ;
    private String id;
    private String name;
}
