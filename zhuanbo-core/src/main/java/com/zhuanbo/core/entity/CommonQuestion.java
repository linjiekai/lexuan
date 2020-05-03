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
 * 常见问题表
 * </p>
 *
 * @author rome
 * @since 2019-06-14
 */
@TableName("shop_common_question")
@Data
public class CommonQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 平台，名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 0：未删除，1：已删除
     */
    private Integer deleted;

    /**
     * 操作人员
     */
    private String operator;

    /**
     * 位置, index:首页
     */
    private String position;

    /**
     * 序号
     */
    private Long serialNumber;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
