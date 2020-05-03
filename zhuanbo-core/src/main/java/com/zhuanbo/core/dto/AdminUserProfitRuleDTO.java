package com.zhuanbo.core.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class AdminUserProfitRuleDTO extends AdminBaseRequestDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 利润分配类型 1：直推 2：销售额
     */
    private Integer profitType;

    /**
     * M体验官利润
     */
    private BigDecimal plus;

    /**
     * M达人利润
     */
    private BigDecimal train;

    /**
     * M达人间接利润
     */
    private BigDecimal trainIndt;

    /**
     * M达人平级利润
     */
    private BigDecimal trainEq;

    /**
     * M司令利润
     */
    private BigDecimal serv;

    /**
     * M司令间接利润
     */
    private BigDecimal servIndt;

    /**
     * M司令平级利润
     */
    private BigDecimal servEq;

    /**
     * M司令下级M达人
     */
    private BigDecimal servLower;

    /**
     *  操作人
     */
    private Integer adminId;

    /**
     * 操作人名称
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
