package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品规格值列表，采用JSON数组格式
     */
    private String[] specifications;

    /**
     * 商品货品价格
     */
    private BigDecimal price;

    /**
     * 商品货品数量
     */
    private Integer stock;

    /**
     * 商品货品图片
     */
    private String url;

    /**
     * 商品类型 0：普通商品 1：会员商品
     */
    private Integer goodsType;

    /**
     * 利润价
     */
    private BigDecimal profitPrice;

    /**
     * 分享价
     */
    private BigDecimal sharePrice;

    private Integer deleted;
}
