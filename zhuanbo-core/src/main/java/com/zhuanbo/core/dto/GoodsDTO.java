package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品dto
 */
@Data
public class GoodsDTO implements Serializable {

    private Integer id;
    /*** 0-商品，1-赠品 */
    private Integer goodsType;
    private String name;
    /*** 附名称 */
    private String sideName;
    private String videoUrl;
    /*** 封面图 */
    private List<String> coverImages;
    /*** 详情图 */
    private List<String> detail;
    /*** 价格 */
    private BigDecimal price;
    /*** 普通用户价格 */
    private BigDecimal plain;
    /*** vip价格 */
    private BigDecimal plus;
    /*** 店长价格 */
    private BigDecimal train;
    /*** 总监价格 */
    private BigDecimal serv;
    /*** 合伙人价格 */
    private BigDecimal partner;
    /*** 联创价格 */
    private BigDecimal director;
    /*** 预留价格 */
    private BigDecimal spokesman;
    private LocalDateTime updateTime;
    /*** 商品状态 [0:下架, 1:上架, 2:缺货] */
    private Integer status;
    private String adminUserName;
    /*** 普通用户价格 */
    private List<AttributesDTO> attributes;
    /*** 普通用户价格 */
    private List<SkuDTO> skus;
    private Integer categoryId;

    /**
     * 规格数据
     */
    @Data
    public static class AttributesDTO implements Serializable{
        private Integer id;
        /*** 规格名称 */
        private String name;
        /*** 规格值列表 */
        private List<SpecificationDTO> specifications;
    }

    /**
     * 规格值
     */
    @Data
    public static class SpecificationDTO implements Serializable{
        /*** 规格值id */
        private Integer id;
        /*** 规格值 */
        private String name;
        /*** 规格图片，第一个规格的规格值都需要图片 */
        private String url;
    }

    /**
     * sku数据
     */
    @Data
    public static class SkuDTO implements Serializable{
        private Integer id;
        /*** sku对应的规格值ID组合 */
        private List<Integer> specificationIds;
    }
}
