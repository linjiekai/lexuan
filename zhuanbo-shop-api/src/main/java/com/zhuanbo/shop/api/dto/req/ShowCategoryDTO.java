package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

@Data
public class ShowCategoryDTO {

	private Long id;
	
    private Long pid;
    
    private Integer type;
}
