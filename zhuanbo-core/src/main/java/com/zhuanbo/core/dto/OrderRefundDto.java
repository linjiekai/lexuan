package com.zhuanbo.core.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRefundDto {

	private String orderNo;
	
	private Integer adminId;
	
	private String operator;
	
	private String remark;
	private BigDecimal reducePrice;
}
