package com.zhuanbo.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author j.t
 * @title: AdStatusEnum
 * @description: 广告状态枚举
 * @date 2019/11/1 15:21
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum AdStatusEnum {

    /**
     * 0:下线
     */
    OFF_LINE(0, "下线"),
    /**
     * 1:待生效
     */
    WAIT_EFFECTIVE(1, "待生效"),
    /**
     * 2:生效中/上线
     */
    ON_LINE(2, "生效中"),

    ;

    private int id;
    private String name;
}
