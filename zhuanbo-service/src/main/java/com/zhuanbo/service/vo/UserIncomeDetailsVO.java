package com.zhuanbo.service.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserIncomeDetailsVO {

	private String headImgUrl;
	
	private String nickname;
	
	private String content;
	
	private Integer operateType;
	
	private BigDecimal operateIncome;
	
	private Integer incomeType;
	
	private String incomeDate;
	
	private String incomeTime;

	private String fromUserId;

	private BigDecimal price;
	
	private Integer statType;
	
	private Integer modeType;

	private Integer changeType;
}
