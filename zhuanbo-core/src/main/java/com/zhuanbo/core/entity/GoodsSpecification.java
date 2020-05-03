package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品规格表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_goods_specification")
@Data
public class GoodsSpecification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品属性id
     */
    private Integer attributeId;

    /**
     * 商品规格名称
     */
    private String name;

    /**
     * 规格图片
     */
    private String url;


    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * -1:删除，1:正常
     */
    private Integer status;


}
