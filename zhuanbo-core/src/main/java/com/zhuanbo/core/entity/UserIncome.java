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
 * 用户收益表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_user_income")
@Data
public class UserIncome implements Serializable {

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
     * 收益总数
     */
    private BigDecimal totalIncome;

    /**
     * 商品销售收益
     */
    private BigDecimal shareIncome;

    /**
     * 自省收益
     */
    private BigDecimal econIncome;

    /**
     * 课时收益
     */
    private BigDecimal trainIncome;

    /**
     * 基础课时收益
     */
    private BigDecimal trainIncomeBase;

    /**
     * 名品课时费
     */
    private BigDecimal trainIncomeMp;

    /**
     * 合伙人课时收益
     */
    private BigDecimal trainIncomePartner;

    /**
     * 不可用/冻结/在途中收益总数
     */
    private BigDecimal totalUavaIncome;

    /**
     * 不可用/冻结/在途中分享收益
     */
    private BigDecimal shareUavaIncome;

    /**
     * 不可用/冻结/在途中自省收益
     */
    private BigDecimal econUavaIncome;

    /**
     * 用户总消费金额
     */
    private BigDecimal totalConsume;

    /**
     * 团队总销售额
     */
    private BigDecimal totalSale;

    /**
     * 团队总人数
     */
    private Integer totalTeam;

    /**
     * 积分总数
     */
    private Integer totalPoint;

    /**
     * 已使用积分
     */
    private Integer usePoint;

    /**
     * 不可用积分/冻结积分
     */
    private Integer uavaPoint;

    /**
     * 剩余可用积分
     */
    private Integer usablePoint;

    /**
     * 最后统计时间 时间戳
     */
    private Long lastTime;

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
