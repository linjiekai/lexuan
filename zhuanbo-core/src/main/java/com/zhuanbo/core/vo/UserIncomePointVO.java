package com.zhuanbo.core.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 * @title: UserIncomePointVO
 * @description: 用户收益积分
 * @date 2020/4/23 22:11
 */
@Data
public class UserIncomePointVO implements Serializable {

    /**
     * 积分总数
     */
    private Integer totalPoint;

    /**
     * 已使用积分
     */
    private Integer usePoint;

    /**
     * 不可用积分/冻结积分
     */
    private Integer uavaPoint;

    /**
     * 剩余可用积分
     */
    private Integer usablePoint;

}
