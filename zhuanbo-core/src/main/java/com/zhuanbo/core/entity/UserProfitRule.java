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
 * 用户利润分配规则表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_user_profit_rule")
@Data
public class UserProfitRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 利润分配类型 1：直推 2：销售额
     */
    private Integer profitType;

    /**
     * 课时费利润分配模式 0：基础 1：名品 2：合伙人
     */
    private Integer modeType;
    
    /**
     * 等级利润分配 json格式
     */
    private String content;

    /**
     * M达人利润
     */
    private BigDecimal plus;

    /**
     * M体验官利润
     */
    private BigDecimal train;

    /**
     * M体验官间接利润
     */
    private BigDecimal trainIndt;

    /**
     * M体验官平级利润
     */
    private BigDecimal trainEq;

    /**
     * M司令利润
     */
    private BigDecimal serv;

    /**
     * M司令间接利润
     */
    private BigDecimal servIndt;

    /**
     * M司令平级利润
     */
    private BigDecimal servEq;

    /**
     * M司令下级M体验官
     */
    private BigDecimal servLower;

    /**
     * 操作人
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
