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
 * 品牌商表
 * </p>
 *
 * @author rome
 * @since 2019-07-26
 */
@TableName("shop_brand")
@Data
public class Brand implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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

    private String operator;
    private String operatorId;
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
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
