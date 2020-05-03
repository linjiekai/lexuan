package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 调怅记录表:操作类型 [0:减少, 1:增加]
 * @date 2019/12/11 14:15
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum IncomeAdjustOperateTypeEnum {

    /**
     * 操作类型:0:减少收益
     */
    SUBSTRACT(0, "减少收益"),
    /**
     * 操作类型:1:增加收益
     */
    ADD(1, "增加收益"),
    ;

    private int id;
    private String name;

    public static String toName(int id) {
        for (IncomeAdjustOperateTypeEnum type : IncomeAdjustOperateTypeEnum.values()) {
            if (id == type.id) {
                return type.name;
            }
        }
        return "";
    }
}
