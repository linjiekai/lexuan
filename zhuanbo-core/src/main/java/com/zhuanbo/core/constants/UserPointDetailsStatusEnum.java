package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户积分收益明细
 *
 * @author Administrator
 * @date 2019/11/8 17:21
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum UserPointDetailsStatusEnum {

    /**
     * 平台收益明细状态:1:有效
     */
    EFFECTIVE(1, "有效"),
    /**
     * 平台收益明细状态:2:已扣除
     */
    DEDUCTED(2, "已扣除"),
    /**
     * 平台收益明细状态:3:已过期
     */
    EXPIRED(3, "已过期"),
    /**
     * 平台收益明细状态:4:冻结中
     */
    FREEZING(4, "冻结中"),
    /**
     * 平台收益明细状态:5:冻结返还
     */
    FROZEN_RETURN(5, "冻结返还"),
    /**
     * 平台收益明细状态:6:冻结扣减
     */
    FROZEN_DEDUCTION(6, "冻结扣减"),
    ;

    private int id;
    private String name;
}
