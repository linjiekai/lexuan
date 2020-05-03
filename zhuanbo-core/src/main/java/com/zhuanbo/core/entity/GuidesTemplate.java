package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 辅助线
 */
@TableName("shop_guides_template")
@Data
public class GuidesTemplate implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long index;
    /**
     * 颜色
     */
    private String color;
    /**
     * 左右边距
     */
    private String cwMargin;
    /**
     * 上下边距
     */
    private String udMargin;
    /**
     * 颜色
     */
    private Long templatePid;
    /**
     * 线条风格 ，0:实线，1：虚线，3:点线
     */
    private Integer lineType;

    private LocalDateTime createTime;
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

}
