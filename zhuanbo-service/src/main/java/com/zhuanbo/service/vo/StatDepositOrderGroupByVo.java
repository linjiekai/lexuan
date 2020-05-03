package com.zhuanbo.service.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatDepositOrderGroupByVo {

	private Integer vsDepositCount;
	
	private BigDecimal vsDepositPrice;

	private Integer orderType; //1:VIP499 2:赚播店长2500 3:赚播总监10000 4:赚播合伙人30000 5:赚播联创90000
	
}
