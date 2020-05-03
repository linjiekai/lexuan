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
 * 字典表
 * </p>
 *
 * @author rome
 * @since 2019-07-01
 */
@TableName("shop_dictionary")
@Data
public class Dictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类别
     */
    private String category;

    /**
     * 名称
     */
    private String name;

    /**
     * 字符串值
     */
    private String strVal;

    /**
     * 长/整形值
     */
    private Long longVal;

    /**
     * 是否展示 0：否 1：是
     */
    private Integer visible;

    /**
     * 描述
     */
    private String description;

    /**
     * 操作人id
     */
    private long adminId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 添加时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
