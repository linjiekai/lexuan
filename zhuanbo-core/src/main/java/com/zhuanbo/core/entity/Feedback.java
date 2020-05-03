package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 反馈表
 * </p>
 *
 * @author rome
 * @since 2019-01-14
 */
@TableName("shop_feedback")
@Data
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 反馈的用户id(lm_user.id)
     */
    private Integer userId;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 图片,json数组格式
     */
    private String[] images;

    /**
     * 状态，0：未处理，1：已处理
     */
    private Boolean status;

    /**
     * 商家返回信息
     */
    private String feedbackContent;

    /**
     * 网络环境
     */
    private String network;

    /**
     * 手机型号
     */
    private String mobileModel;

    /**
     * 手机系统版本
     */
    private String mobileSystemVersion;

    /**
     * APP版本
     */
    private String appVersion;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 添加时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
