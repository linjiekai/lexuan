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
 * 升级费明细表
 * </p>
 *
 * @author rome
 * @since 2019-07-31
 */

@TableName("shop_upgrade_details")
@Data
public class UpgradeDetails implements Serializable {

	private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 商城订单号
     */
    private String orderNo;

    /**
     * 充值日期yyyy-MM-dd
     */
    private String orderDate;

    /**
     * 充值时间HH:mm:ss
     */
    private String orderTime;

    /**
     * 商城会员ID
     */
    private Long userId;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 支付日期yyyy-MM-dd
     */
    private String payDate;

    /**
     * 支付时间HH:mm:ss
     */
    private String payTime;
    
    /**
     * 支付方式 0：线下 1：线上
     */
    private Integer payType;

    /**
     * 是否退款 0：否 1：是
     */
    private Integer refundFlag;

    /**
     * 操作人ID
     */
    private Integer adminId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    
}
