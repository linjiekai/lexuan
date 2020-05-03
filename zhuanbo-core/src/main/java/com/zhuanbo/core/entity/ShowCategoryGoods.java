package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 展示分类首页商品
 * </p>
 *
 * @author rome
 * @since 2019-08-12
 */
@TableName("shop_show_category_goods")
@Data
public class ShowCategoryGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long showCategoryId;

    private Long goodsId;

    private LocalDateTime addTime;
}
