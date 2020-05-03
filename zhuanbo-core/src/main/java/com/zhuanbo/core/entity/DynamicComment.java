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
 * 动态评论表
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
@TableName("shop_dynamic_comment")
@Data
public class DynamicComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论的父id
     */
    private Long pid;

    /**
     * 动态的id
     */
    private Long dynamicId;

    /**
     * 内容
     */
    private String content;

    /**
     * 删除。0：未删除，1：删除
     */
    private Integer deleted;

    /**
     * 评论人id
     */
    private Long fromUid;

    /**
     * 被回复人id
     */
    private Long toUid;

    private Integer checked;// 审核 0：未审核、1：已审核

    private String operator;// 审核操作人

    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
