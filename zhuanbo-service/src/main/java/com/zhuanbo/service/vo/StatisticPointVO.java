package com.zhuanbo.service.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 * @title: StatisticPointVO
 * @description: 积分统计vo
 * @date 2020/4/28 10:46
 */
@Data
public class StatisticPointVO implements Serializable {

    /**
     * 总充值积分
     */
    private Integer allPoint;

    /**
     * 已使用积分
     */
    private Integer usePoint;

    /**
     * 剩余积分
     */
    private Integer residualPoint;


}
