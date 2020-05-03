package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户收益:操作类型:[1:增加收益, 2:减少收益]
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum UserIncomeOperateType {

    /*** 操作类型:1:增加收益 */
    ADD(1, "增加收益"),
    /*** 操作类型:2:扣减收益 */
    SUBSTRACT(2, "扣减收益"),
    ;

    private int id;
    private String name;

    public static UserIncomeOperateType parse(int id) {
        for (UserIncomeOperateType type : UserIncomeOperateType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
