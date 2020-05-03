package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Administrator
 * @title: LevelChangeRecode
 * @description: 用户邀请关系变更记录
 * @date 2020/4/27 11:11
 */
@Data
@Builder
@TableName("shop_level_change_recode")
public class LevelChangeRecode implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户.id
     */
    private Long userId;

    /**
     * 上级用户id
     */
    private Long pid;

    /**
     * 用户等级
     */
    private Integer ptLevel;

    /**
     * 旧的用户等级
     */
    private Integer oldPtLevel;

    /**
     * 支付类型 [1:积分]
     */
    private Integer payType;

    /**
     * 支付金额/积分
     */
    private BigDecimal price;

    /**
     * 操作人id
     */
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    private String remark;
}
