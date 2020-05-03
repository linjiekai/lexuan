package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 调怅记录表
 * </p>
 *
 * @author rome
 * @since 2019-09-24
 */
@TableName("shop_user_income_adjust")
@Data
public class AdjustAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被调账用户
     */
    private Long userId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 调账表订单号
     */
    private String adjustNo;

    /**
     * 单类型，0：交易，1：充值
     */
    private Integer orderType;

    private BigDecimal price;
    /**
     * 调账类目。0：可提收益
     */
    private Integer adjustCategory;

    /**
     * 变更类型。0：调账
     */
    private Integer adjustType;

    /**
     * 类型。0：减少、1：增加
     */
    private Integer operateType;


    /**
     * 调账发起人
     */
    private Long adjustUserId;

    /**
     * 原因
     */
    private String reason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人
     */
    private Long operatorId;

    /**
     * 操作人
     */
    private String operator;

    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
