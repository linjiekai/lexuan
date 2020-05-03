package com.zhuanbo.core.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Jiekai Lin
 * @Description(描述):  充值DTO
 * @date: 2019/10/25 13:52
 */
@Data
public class MobileDepositDTO extends MobileBaseParamsDTO {

    private String tradeCode;
    private String busiType;
    private String sysCnl;
    private String clientIp;
    private Integer buyType = 0;//
    private String inviteCode;
    private BigDecimal price;
    private Integer orderType;
}
