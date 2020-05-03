package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class StatUserTeamDTO extends BaseParamsDTO{

	private List<Long> userIds;
	
	private Integer ptLevel;

	private Integer ptOperLevel;

}
