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
 * 用户收益明细表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_user_income_details")
@Data
public class UserIncomeDetails implements Serializable {

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
     * 订单号
     */
    private String orderNo;

    /**
     * 变更类型 0:充值, 1:退款, 2:调账, 3:奖励, 4:提现
     */
    private Integer changeType;
    
    /**
     * 奖励类型 1:商品销售奖励 2:服务商销售奖励
     */
    private Integer rewardType;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long fromUserId;

    /**
     * 用户等级0:普通用户;1:VIP;2:县级店;3:品牌店;4:金钻;5:总裁;6:分公司
     */
    private Integer fromPtLevel;

    /**
     * 源订单号, 退款放交易订单号，收益放充值订单号
     */
    private String sourceOrderNo;

    /**
     * 调账订单号
     */
    private String adjustNo;

    /**
     * 操作收益 本次操作的收益值
     */
    private BigDecimal operateIncome;

    /**
     * 可用收益
     */
    private BigDecimal usableIncome;

    /**
     * 操作类型 [1:增加收益, 2:减少收益]
     */
    private Integer operateType;

    /**
     * 收益类型  1:商品销售奖励 2:服务商销售奖励 3:下级销售扣减 4：提现
     */
    private Integer incomeType;

    /**
     * 银行编号
     */
    private String bankCode;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

    /**
     * 收益状态 1：有效 2：已扣除 3：已过期 4：冻结中 5：冻结返还 6：冻结扣减
     */
    private Integer status;

    /**
     * 登记日期yyyy-MM-dd
     */
    private String incomeDate;

    /**
     * 登记时间HH:mm:ss
     */
    private String incomeTime;

    /**
     * 利润分配类型 1：直推 2：销售额
     */
    private Integer profitType;

    /**
     * 利润规则
     */
    private BigDecimal profitRule;

    /**
     * 统计类型 0：在途 1：累计
     */
    private Integer statType;

    /**
     * 统计日期yyyy-MM-dd
     */
    private String statDate;

    /**
     * 收益来源  当扣减收益时，存储扣减的收益记录id，用于追溯收益 json格式
     */
    private String incomeSource;

    /**
     * 过期时间 时间戳
     */
    private Long expTime;

    /**
     * 内容
     */
    private String content;

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
