package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Administrator
 * @title: MobileWithdrConfigDTO
 * @description: 提现字典配置类
 * @date 2020/4/2 13:53
 */
@Data
public class WithdrDicDTO implements Serializable {

    /**
     * 可提现银行是否开放 [0:关闭, 1:开放]
     */
    private Long bankOpen;

    /**
     * 提现手续费 [千分比]
     */
    private Long commisionRatio;

    /**
     * 提现最大金额
     */
    private BigDecimal priceMax;

    /**
     * 提现最低金额
     */
    private BigDecimal priceMin;

    /**
     * 1天提现2次
     */
    private Long timesMax;

    /**
     * 是否开放(提现到银行卡) [0:关闭, 1:开放]
     */
    private Long toBankOpen;

    /**
     * 微信提现渠道开关 [0:关闭, 1:开放]
     */
    private Long toWeixinOpen;

    /**
     * 平台每日提现限额 (单位:元)
     */
    private BigDecimal platformDayLimit;

    /**
     * 单人单日提现限额 (单位:元)
     */
    private BigDecimal personDayLimit;

    /**
     * 单人单笔提现限额 (单位:元)
     */
    private BigDecimal personSingleLimit;
}
