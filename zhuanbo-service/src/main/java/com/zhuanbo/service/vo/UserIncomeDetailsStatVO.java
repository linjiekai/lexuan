package com.zhuanbo.service.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserIncomeDetailsStatVO {

	private Integer incomeCount;
	private BigDecimal operateIncome;
	private Integer incomeType;
	private Integer modeType;
	private Integer operateType;
}
