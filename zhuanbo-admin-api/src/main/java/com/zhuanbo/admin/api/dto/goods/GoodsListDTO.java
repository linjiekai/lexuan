package com.zhuanbo.admin.api.dto.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsListDTO {

    private Integer id;// 商品ID
    private String goodsSn;// 商品编号
    private String name;// 商品主标题
    private String sideName;// 商品副标题
    private List<Object> specifications;// 商品属性、规格
    private List<Object> specificationsUrl;// 商品属性、规格图片
    private String[] videoUrl;// 规格封面图和视频
    private String[] coverImages;// 商品封面（图片）,顶部滑动
    private String[] detail;// 商品详情页（图片）
    private String brief;// 商品简介
    private String unit;// 商品单位，例如件、盒
    private BigDecimal price;// 商品价格
    private Integer status;// 商品状态：-1删除 0：下架  1：上架 2:缺货
    private String operator;// 操作人
    private LocalDateTime addTime;
    private LocalDateTime updateTime;
    private Integer salesNum;// 销量
    private Integer initSalesNum;// 默认初始销量
    private BigDecimal profitPrice;// 利润价
    private BigDecimal sharePrice;// 分享价
    private Integer goodsType;// 商品类型 0：普通商品 1：会员商品
    private Long categoryId;
    /**
     * 商品品牌
     */
    private Integer brandId;

    /**
     * 商品别名
     */
    private String alias;
    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 分类名称
     */
    private String categoryName;
    private BigDecimal originalPrice;
    private BigDecimal baseScore;
    private Integer traceType;
    private String supplierCode;
    private String skuCode;
    /**
     * 是否600会员礼包
     */
    private Integer buyerPartner;
}
