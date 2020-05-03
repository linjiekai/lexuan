package com.zhuanbo.core.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserIncomeDetailDTO extends AdminBaseRequestDTO {
    /**
     * 主键ID
     */
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
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long fromUserId;

    /**
     * 充值订单号
     */
    private String depositNo;

    /**
     * 操作收益 本次操作的收益值
     */
    private BigDecimal operateIncome;

    /**
     * 可用收益
     */
    private BigDecimal usableIncome;

    /**
     * 操作类型 1：增加收益 2：减少收益
     */
    private Integer operateType;

    /**
     * 收益类型  1： 消费送 2：提现扣减 3:拉新收益  9：过期等
     */
    private Integer incomeType;

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
     * 利润分配类型 0：直推 1：销售额
     */
    private Integer profitType;

    /**
     * 利润规则
     */
    private BigDecimal profitRule;

    /**
     * 统计类型 0：未统计 1：已统计
     */
    private Integer statType;

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 会员模式类型 0:基础 1：运营商
     */
    private Integer modeType;

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
    private LocalDateTime updateTime;

    /**
     * 提现方式。1：支付宝，2：微信，3：银行卡
     */
    private Integer withDrawType;
    /**
     * 提现卡号
     */
    private String withDrawCardNo;

}
