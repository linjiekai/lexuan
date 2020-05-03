package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 * @title: DepositOrderTypeSplitEnum
 * @description: TODO
 * @date 2020/4/24 12:38
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DepositOrderTypeSplitEnum {

    /*** 1:499  */
    TYPE_SPLIT_1(1, 499),
    /*** 2:2500  */
    TYPE_SPLIT_2(2, 2500),
    /*** 3:10000  */
    TYPE_SPLIT_3(3, 10000),
    /*** 4:90000  */
    TYPE_SPLIT_4(4, 90000),
    /*** 5:300000  */
    TYPE_SPLIT_5(5, 300000),
    ;
    private int id;
    private int point;
}
