package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @title: PlatformIncomeDetailsDTO
 * @date 2019/11/8 16:18
 */
@Data
public class AdminPlatformIncomeDetailsDTO extends AdminBaseRequestDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户姓名
     */
    private String nickname;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单类型 0：交易 1：退款
     */
    private Integer orderType;

    /**
     * 原订单号
     */
    private String sourceOrderNo;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 操作类型 1：增加收益 2：减少收益
     */
    private Integer operateType;

    /**
     * 收益类型  1： 购买订单 2：云仓订单 3:提货到家   4:保证金 5:提现
     */
    private Integer incomeType;

    /**
     * 收益状态 1：有效 2：已扣除 3：已过期 4：冻结中 5：冻结返还 6：冻结扣减
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
