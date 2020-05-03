package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum InviteCodeUseStatusEnum {
    /**
     * 0:未使用
     */
    UNUSED(0, "未使用"),
    /**
     * 1:已使用
     */
    USED(1, "已使用"),
    ;
    private int id;
    private String name;
}
