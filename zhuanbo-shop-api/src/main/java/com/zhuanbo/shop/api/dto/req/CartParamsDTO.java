package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class CartParamsDTO extends BaseParamsDTO {
    private Integer productId;
    private Integer number;
    private List<Integer> cartIds;
    private Integer buyType;
}
