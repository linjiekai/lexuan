package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 头像图片表
 * </p>
 *
 * @author rome
 * @since 2019-06-17
 */
@TableName("shop_head_img")
@Data
public class HeadImg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 头像url地址
     */
    private String headImgUrl;

    private LocalDateTime addTime;
}
