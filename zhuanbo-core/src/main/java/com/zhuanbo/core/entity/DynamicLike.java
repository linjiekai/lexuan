package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 动态点赞记录表
 * </p>
 *
 * @author rome
 * @since 2019-04-11
 */
@TableName("shop_dynamic_like")
@Data
public class DynamicLike implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 动态id
     */
    private Long dynamicId;

    /**
     * 用户id
     */
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

}
