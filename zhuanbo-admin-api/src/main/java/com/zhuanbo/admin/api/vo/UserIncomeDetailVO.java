package com.zhuanbo.admin.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserIncomeDetailVO {
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
     * 昵称
     */
    private String nickname;

    /**
     * 用户等级
     */
    private Integer ptLevel;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long fromUserId;

    /**
     * 操作收益 本次操作的收益值
     */
    private BigDecimal operateIncome;

    /**
     * 变更类型 0:充值, 1:退款, 2:调账, 3:奖励, 4:提现
     */
    private Integer changeType;

    /**
     * 操作类型 1：增加收益 2：减少收益
     */
    private Integer operateType;

    /**
     * 收益类型  1： 消费送 2：提现扣减 3:拉新收益  9：过期等
     */
    private Integer incomeType;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 提现方式。1：支付宝，2：微信，3：银行卡
     */
    private Integer withDrawType;

    /**
     * 提现卡号
     */
    private String bankCardNo;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

}
