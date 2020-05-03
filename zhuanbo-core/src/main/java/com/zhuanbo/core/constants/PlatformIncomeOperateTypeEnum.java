package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 平台收益明细:操作类型
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PlatformIncomeOperateTypeEnum {

    /**
     * 操作类型:1:增加收益
     */
    ADD(1, "增加收益"),
    /**
     * 操作类型:2:减少收益
     */
    SUBSTRACT(2, "减少收益"),
    ;

    private int id;
    private String name;

}
