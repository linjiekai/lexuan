package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 收益日统计报表
 * </p>
 *
 * @author rome
 * @since 2019-06-20
 */
@Data
public class StatIncomeDay implements Serializable {


    private static final long serialVersionUID = 1L;

	/**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 订单数量
     */
    private Integer vsOrderCount = 0;

    /**
     * 订单金额
     */
    private BigDecimal vsOrderPrice = new BigDecimal(0);

    /**
     * 累计收益
     */
    private BigDecimal vsTotalIncome = new BigDecimal(0);

    /**
     * 提现金额
     */
    private BigDecimal vsWithdrIncome = new BigDecimal(0);

    /**
     * 总用户数
     */
    private Integer vsTotalUserCount = 0;

    /**
     * 新增用户
     */
    private Integer vsUserCount ;

    /**
     * 新增邀请关系
     */
    private Integer vsUserInviteCount = 0;

    /**
     * 499销售额
     */
    private BigDecimal vsDepositPriceOne = new BigDecimal(0);

    /**
     * 499订单数量
     */
    private Integer vsDepositCountOne = 0;

    /**
     * 2500销售额
     */
    private BigDecimal vsDepositPriceTwo = new BigDecimal(0);

    /**
     * 2500订单数量
     */
    private Integer vsDepositCountTwo = 0;

    /**
     * 10000销售额
     */
    private BigDecimal vsDepositPriceThree = new BigDecimal(0);

    /**
     * 10000订单数量
     */
    private Integer vsDepositCountThree = 0;

    /**
     * 30000销售额
     */
    private BigDecimal vsDepositPriceFour = new BigDecimal(0);

    /**
     * 30000订单数量
     */
    private Integer vsDepositCountFour = 0;

    /**
     * 90000销售额
     */
    private BigDecimal vsDepositPriceFive = new BigDecimal(0);

    /**
     * 90000订单数量
     */
    private Integer vsDepositCountFive = 0;

    /**
     * 商品销售奖励
     */
    private BigDecimal vsIncomeTypeOne = new BigDecimal(0);

    /**
     * 服务商销售奖励
     */
    private BigDecimal vsIncomeTypeTwo = new BigDecimal(0);

    /**
     * 下级销售扣减
     */
    private BigDecimal vsIncomeTypeThree = new BigDecimal(0);

    /**
     * 进货差价奖励
     */
    private BigDecimal vsIncomeTypeFive = new BigDecimal(0);

    /**
     * 下级运费扣减
     */
    private BigDecimal vsIncomeTypeSix = new BigDecimal(0);

}
