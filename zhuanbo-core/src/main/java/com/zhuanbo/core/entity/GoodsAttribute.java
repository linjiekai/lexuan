package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品参数表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_goods_attribute")
@Data
public class GoodsAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品属性名称
     */
    private String name;


    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

}
