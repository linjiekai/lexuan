package com.zhuanbo.core.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderProfitResultDTO {
    private String orderNo;
    private Long profitUserId;
    private BigDecimal profitAmount;
    private Integer profitType;
    private Integer rewardType;
    private Integer operateType;
}
