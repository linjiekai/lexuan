package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 推送表
 * </p>
 *
 * @author rome
 * @since 2019-04-11
 */
@TableName("shop_push")
@Data
public class Push implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 跳转位置,商品id或动态id
     */
    private Long targetId;

    /**
     * 跳转位置：活动url
     */
    private String targetUrl;

    /**
     * 类型。0:商品、1:活动链接'、2：动态
     */
    private Integer type;

    /**
     * 推送时间
     */
    private LocalDateTime pushTime;

    /**
     * 状态 0：待推送、1已推送、2下线
     */
    private Integer status;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 第三方消息状态id
     */
    private String taskId;
    /**
     * 第三方消息状态
     * 0-排队中, 1-发送中，2-发送完成，3-发送失败，4-消息被撤销5-消息过期, 6-筛选结果为空，7-定时任务尚未开始处理
     */
    private Integer taskStatus;

    private String platform;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
