package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 推送接收者表
 * </p>
 *
 * @author rome
 * @since 2019-04-11
 */
@TableName("shop_push_receiver")
@Data
public class PushReceiver implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 推送id
     */
    private Long pushId;

    /**
     * 接收者id
     */
    private Long userId;

    /**
     * 接收状态。0：未接收、1：已接收
     */
    private Integer stauts;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
