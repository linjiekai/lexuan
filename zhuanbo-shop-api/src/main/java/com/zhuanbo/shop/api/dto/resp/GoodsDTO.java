package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsDTO {
    private static final long serialVersionUID = 1L;
    private Integer id;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品名称
     */
    private String name;

    private String sideName;

    /**
     * 商品所属类目ID
     */
    private Long categoryId;

    private Integer brandId;

    /**
     * 商品页面商品图片或者视频
     */
    private String[] videoUrl;

    private String videoImage;

    /**
     * 商品宣传图片列表，采用JSON数组格式
     */
    private String[] coverImages;

    private String[] detail;

    /**
     * 商品简介
     */
    private String brief;


    /**
     * 商品单位，例如件、盒
     */
    private String unit;
    /**
     * 价格
     */
    private BigDecimal price;

    /**
     *商品分享链接
     */
    private String shareUrl;


    /**
     * 商品状态： 0：下架  1：上架 2:缺货
     */
    private Integer status;

    /**
     * 购买数量
     */
    private Integer salesNum;

    /**
     * 初始值
     */
    private Integer initSalesNum;

    /**
     * 商品类型 0：普通商品 1：会员商品
     */
    private Integer goodsType;
    
    /**
     * 是否600会员礼包
     */
    private Integer buyerPartner;

    /**
     * 利润价
     */
    private BigDecimal profitPrice;

    /**
     * 分享价
     */
    private BigDecimal sharePrice;

    private Integer deleted;
    private BigDecimal originalPrice;
    private BigDecimal baseScore;
    private Integer traceType;
    private String supplierCode;
}
