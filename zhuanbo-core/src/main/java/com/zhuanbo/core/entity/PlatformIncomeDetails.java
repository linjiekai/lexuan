package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 平台收益明细表
 * </p>
 *
 * @author rome
 * @since 2019-10-29
 */
@Data
@TableName("shop_platform_income_details")
public class PlatformIncomeDetails implements Serializable {

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
     * 订单类型 [0:交易, 1:退款]
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
     * 操作类型 [1:增加收益, 2:减少收益]
     */
    private Integer operateType;

    /**
     * 收益类型 [1:购买订单, 2:充值, 3:提现]
     */
    private Integer incomeType;

    /**
     * 收益状态 [1:有效, 2:已扣除, 3:已过期, 4:冻结中, 5:冻结返还, 6:冻结扣减]
     */
    private Integer status;

    /**
     * 登记日期 yyyy-MM-dd
     */
    private String incomeDate;

    /**
     * 登记时间 HH:mm:ss
     */
    private String incomeTime;

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

}
