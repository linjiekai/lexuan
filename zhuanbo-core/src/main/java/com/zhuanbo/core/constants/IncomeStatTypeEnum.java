package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户收益明细:统计类型 0：未统计(或在途) 1：已统计（或累计）
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum IncomeStatTypeEnum {

    /**
     * 收益明细统计类型:0:未统计(或在途)
     */
    NO(0, "未统计"),
    /**
     * 收益明细统计类型:1:已统计（或累计）
     */
    YES(1, "已统计"),
    ;

    private int id;
    private String name;
}
