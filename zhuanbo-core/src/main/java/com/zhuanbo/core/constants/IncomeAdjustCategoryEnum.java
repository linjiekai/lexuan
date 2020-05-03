package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 调怅记录表:调账类目 [0:可提收益]
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum IncomeAdjustCategoryEnum {

    WITHDRAWABLE(0, "可提收益"),
    ;

    private int id;
    private String name;
}
