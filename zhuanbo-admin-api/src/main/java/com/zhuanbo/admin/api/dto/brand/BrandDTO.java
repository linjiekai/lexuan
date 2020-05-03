package com.zhuanbo.admin.api.dto.brand;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BrandDTO {

    private Long id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * logo
     */
    private String logo;

    /**
     * 明星封面
     */
    private String starCover;

    /**
     * 品牌详情封面
     */
    private String detailCover;

    /**
     * 品牌简介
     */
    private String content;

    /**
     * 序号
     */
    private Integer indexs;

    /**
     * 状态：0：下线，1：上线
     */
    private Integer status;
    /**
     * 逻辑删除，0：未删除，1：删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */

    private LocalDateTime updateTime;

    /**
       商品列表
     */
    private List<GoodDTO> goodsList;

    /**
     * 操作人名称
     */
    private String operator;

    /**
     * 商品id列表
     */
    private Integer[] goodsId;

    /**
     * 操作人id
     */
    private String operatorId;
}
