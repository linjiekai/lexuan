package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 类目关联表
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
@TableName("shop_category_relation")
@Data
public class CategoryRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联show_category表ID
     */
    private Long showId;

    /**
     * 关联商品类目表ID
     */
    private Long categoryId;

    /**
     * 关联商品表ID
     */
    private Long goodsId;
    
}
