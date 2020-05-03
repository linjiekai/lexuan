package com.zhuanbo.shop.api.dto.req;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DepositDTO {

    private String platform = "zhuanbo";
    private String tradeType;
    private String tradeCode;
    private String busiType;
    private String sysCnl;
    private String clientIp;
    private Integer buyType = 0;
    private String inviteCode;
    private BigDecimal price;
    private Integer orderType;
    private String couponSn;
    private Long userId;
    private Integer buyNum;
    private String buyCode;
    
}
