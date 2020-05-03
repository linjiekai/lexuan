package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 通知消息池表
 * </p>
 *
 * @author rome
 * @since 2019-06-14
 */
@TableName("shop_notify_msg_pool")
@Data
public class NotifyMsgPool implements Serializable {

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
     * 用户等级
     */
    private Integer ptLevel;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String headImgUrl;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 消息类型 1:真实信息 2:虚拟信息
     */
    private Integer msgType;
    /**
     * 状态 1：生效中 2：已失效
     */
    private Integer status;

    /**
     * 内容
     */
    private String content;

    /**
     * 日期yyyy-MM-dd
     */
    private String msgDate;

    /**
     * 时间HH:mm:ss
     */
    private String msgTime;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
