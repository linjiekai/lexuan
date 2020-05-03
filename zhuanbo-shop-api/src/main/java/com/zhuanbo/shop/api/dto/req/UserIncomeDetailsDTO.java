package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

import java.util.Set;

@Data
public class UserIncomeDetailsDTO extends BaseParamsDTO{

	private Set<Integer> incomeTypes; //收益类型
	private Integer statType;//统计类型
	private Set<Long> userIds;
	private String startDate;
	private String endDate;
	private Integer operateType;//操作类型
	private Set<Integer> changeTypes; //变更类型 0.购买商品、1.运营后台录入原因（调账）、2.提现、3.订单退货、4.身份升级
}
