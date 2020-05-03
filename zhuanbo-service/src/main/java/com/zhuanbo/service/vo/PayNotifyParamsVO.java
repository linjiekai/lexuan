package com.zhuanbo.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PayNotifyParamsVO {
	
	private String orderNo;

    private String orderDate;

    private String orderTime;

    private String payNo;

    private String mercId;

    private String orderStatus;

    private String tradeType;

    private String userId;

    private BigDecimal price;

    private String bankCode;

    private String payDate;

    private String payTime;

	private String openId;

    private BigDecimal reducePrice;
}
