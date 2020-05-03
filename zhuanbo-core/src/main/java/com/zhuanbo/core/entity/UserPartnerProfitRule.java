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
 * 合伙人利润分配规则表
 * </p>
 *
 * @author rome
 * @since 2019-08-20
 */
@TableName("shop_user_partner_profit_rule")
@Data
public class UserPartnerProfitRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 利润分配类型 [1:销售, 2:投资, 联创]
     */
    private Integer profitType;

    /**
     * 利润分配层级 [0:差价, 1:一代, 2:二代, 3:三代]
     */
    private Integer profitLevel;

    /**
     * vip利润
     */
    private BigDecimal vip;

    /**
     * 店长利润
     */
    private BigDecimal storeManager;

    /**
     * 总监利润
     */
    private BigDecimal director;

    /**
     * 合伙人利润
     */
    private BigDecimal partner;

    /**
     * 联创利润
     */
    private BigDecimal base;

    /**
     * 操作人ID
     */
    private Integer adminId;

    /**
     * 操作人
     */
    private String operator;

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
