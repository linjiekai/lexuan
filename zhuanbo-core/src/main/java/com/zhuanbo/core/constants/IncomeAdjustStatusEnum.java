package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 调怅记录表:状态:[0:待处理, 1:处理成功, 2:处理失败]'
 * @date 2019/12/13 14:41
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum IncomeAdjustStatusEnum {

    /**
     * 操作类型:0:减少收益
     */
    WAIT(0, "待处理"),
    /**
     * 操作类型:1:增加收益
     */
    SUCCESS(1, "处理成功"),
    /**
     * 操作类型:1:增加收益
     */
    FAIL(2, "处理失败"),
    ;

    private int id;
    private String name;
}
