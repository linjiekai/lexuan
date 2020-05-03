package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

@Data
public class CategoryDTO {

	private Long id;
	
    private Long pid;
    
    private Integer type;
}
