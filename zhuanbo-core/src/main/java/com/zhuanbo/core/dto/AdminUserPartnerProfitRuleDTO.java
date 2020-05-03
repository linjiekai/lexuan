package com.zhuanbo.core.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserPartnerProfitRuleDTO extends AdminBaseRequestDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 利润分配类型 1：直推 2：销售额
     */
    private Integer profitType;

    /**
     * 套餐名额
     */
    private Integer userNum;

    /**
     * 套餐内推荐奖励金额
     */
    private BigDecimal effIncome;

    /**
     * 合伙人利润
     */
    private BigDecimal partner;

    /**
     * 高级合伙人利润
     */
    private BigDecimal highPartner;

    /**
     * 高级合伙人间接利润
     */
    private BigDecimal highPartnerIndt;

    /**
     * 总监利润
     */
    private BigDecimal director;

    /**
     * 总监间接利润
     */
    private BigDecimal directorIndt;

    /**
     * 高级总监利润
     */
    private BigDecimal highDirector;

    /**
     * 代言人利润
     */
    private BigDecimal spokesman;

    /**
     * 高级代言人利润
     */
    private BigDecimal highSpokesman;

    /**
     * 操作人ID
     */
    private Integer adminId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
