package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartGoodsDTO {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 用户表的用户ID
     */
    private Integer userId;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品货品表的货品ID
     */
    private Integer productId;

    /**
     * 商品货品的价格
     */
    private BigDecimal price;

    /**
     * 商品货品的数量
     */
    private Integer number;

    /**
     * 商品规格值列表，采用JSON数组格式
     */
    private String[] specifications;

    /**
     * 商品图片或者商品货品图片
     */
    private String picUrl;

    /**
     * 购物车中商品状态
     */
    private Integer status;

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
    
    /**
     * 贸易类型。0：一般贸易、1：保税/香港直邮、2：海外直邮（非韩澳）、3：海外直邮（韩澳）
     */
    private Integer traceType;
}
