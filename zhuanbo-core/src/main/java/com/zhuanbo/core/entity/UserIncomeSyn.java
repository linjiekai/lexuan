package com.zhuanbo.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * <p>
 * 用户收益同步表
 * </p>
 *
 * @author rome
 * @since 2020-03-23
 */
@TableName("shop_user_income_syn")
@Data
public class UserIncomeSyn implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long profitUserId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 价格
     */
    private BigDecimal profitAmount;

    /**
     * 收益类型 1:一代 2:二代 3:三代 4:差价 5:越级 6:拉新给予（+10） 7:拉新扣减（-10）8:消耗码的全部返还
     */
    private Integer profitType;

    /**
     * 奖励类型 1:商品销售奖励 2:服务商销售奖励
     */
    private Integer rewardType;

    /**
     * 奖励类型 1:商品销售奖励 2:服务商销售奖励
     */
    private Integer operateType;
    
    private Integer incomeType;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
