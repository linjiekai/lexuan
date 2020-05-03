package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_order")
@Data
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 合伙人等级
     */
    private Integer ptLevel;

    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 
     */
    private String mercId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单流水日期
     */
    private String orderDate;

    /**
     * 订单流水时间
     */
    private String orderTime;

    /**
     * 订单类型 0:购买订单 1:赠送订单
     */
    private Integer orderType;

    /**
     * 订单流水状态 未支付/待支付:W 已完成:S 已取消:C 待发货:WS 待收货:WD
     */
    private String orderStatus;

    /**
     * 订单总额
     */
    private BigDecimal totalPrice;

    /**
     * 应付金额
     */
    private BigDecimal price;

    /**
     * 支付号
     */
    private String payNo;

    /**
     * 支付日期 yyyyMMdd
     */
    private String payDate;

    /**
     * 支付日期 HHmmss
     */
    private String payTime;

    /**
     * 退款金额
     */
    private BigDecimal refundPrice;


    /**
     * 下单终端渠道：WAP、IOS、ANDROID、WEB、H5、MP、WECHAT
     */
    private String sysCnl;

    /**
     * 购买类型 0：普通购买 1：分享购买
     */
    private Integer buyType;

    /**
     * 来源
     */
    private String source;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 幸福狐狸：XFHL
     */
    private String bankCode;

    /**
     * 交易类型 JSAPI：公众号或小程序支付 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付
     */
    private String tradeType;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 失效时间
     */
    private Long expTime;

    /**
     * 邀请人userId
     */
    private Long inviteUserId;

    /**
     * 用户客户端IP
     */
    private String clientIp;

    /**
     * 返回码
     */
    private String payRespCode;

    /**
     * 返回描述
     */
    private String payRespMsg;

    /**
     * 备注
     */
    private String remark;

    /**
     * 供应商
     */
    private Long supplierId;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    // 辅助字段 订单订单商品信息
    @TableField(exist = false)
    private List<OrderGoods> orderGoods;
    
	// 进货类型
    private Integer purchType;
    
    /**
     * 源订单号
     */
    @TableField(exist = false)
    private String sourceOrderNo;

    /**
     * 银行卡号
     */
    @TableField(exist = false)
    private String bankCardNo;

    /**
     * 银行卡姓名
     */
    @TableField(exist = false)
    private String bankCardName;

    /**
     * 调账订单号
     */
    @TableField(exist = false)
    private String adjustNo;
    /**
     * 付款方式
     */
    private String bankName;
    /**
     * 付款账号,银行用户标识
     */
    private String openId;
    /**
     * 随机立减金额
     */
    private BigDecimal reducePrice;

}
