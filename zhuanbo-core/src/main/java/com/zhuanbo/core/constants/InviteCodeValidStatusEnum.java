package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum InviteCodeValidStatusEnum {
    /**
     * 0:无效
     */
    INVALID(0, "无效"),
    /**
     * 1:有效
     */
    VALID(1, "有效"),
    ;
    private int id;
    private String name;
}
