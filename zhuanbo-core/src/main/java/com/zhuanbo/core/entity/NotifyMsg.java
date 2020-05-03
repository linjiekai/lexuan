package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 通知消息表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_notify_msg")
@Data
public class NotifyMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 标题
     */
    private String title;

    /**
     * 消息标记 1:系统通知
     */
    private Integer msgFlag;

    /**
     * 是否已读 0：未读 1：已读
     */
    private Integer readFlag;

    /**
     * 状态 1：生效中 2：已失效
     */
    private Integer status;

    /**
     * 日期yyyy-MM-dd
     */
    private String msgDate;

    /**
     * 时间HH:mm:ss
     */
    private String msgTime;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
