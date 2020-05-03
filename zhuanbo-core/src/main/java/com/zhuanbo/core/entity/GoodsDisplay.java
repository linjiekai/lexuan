package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品橱窗表
 * </p>
 *
 * @author rome
 * @since 2019-06-17
 */
@TableName("shop_goods_display")
@Data
public class GoodsDisplay implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 直播群组id
     */
    private Long liveGroupId;

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 类型[0:普通商品, 1:爆款]
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    
}
