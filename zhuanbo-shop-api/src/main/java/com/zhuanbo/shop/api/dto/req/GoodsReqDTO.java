package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

@Data
public class GoodsReqDTO extends BaseParamsDTO{
    private Integer goodsType;
    private Integer buyerPartner;
    private Integer productId;
    private Long showCategoryId;
    private Long brandId;

    /**
     *  0：销量排序；1：基础分排序；2：价格升序；3：价格降序
     */
    private String sort ="0";
}
