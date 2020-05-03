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
 * 提现订单表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_withdr_order")
@Data
public class WithdrOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 商城订单日期
     */
    private String orderDate;

    /**
     * 商城订单时间
     */
    private String orderTime;

    /**
     * 订单流水状态 A:审核中, W:待提现, S:提现成功, F:提现失败, R:审核拒绝
     */
    private String orderStatus;

    /**
     * 提现金额
     */
    private BigDecimal price;
    
    /**
     * 实际提现金额
     */
    private BigDecimal withdrPrice;
    
    /**
     * 提现手续费
     */
    private BigDecimal withdrRatio;

    /**
     * 提现日期 YYYY-MM-DD
     */
    private String bankWithdrDate;

    /**
     * 提现时间 HH:mm:ss
     */
    private String bankWithdrTime;

    /**
     * 开户行银行联行号
     */
    private String bankNo;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 银行卡类型 01:借记卡;02:信用卡;08:第三方平台
     */
    private String bankCardType;

    /**
     * 校验用户姓名 NO_CHECK：不校验真实姓名 FORCE_CHECK：强校验真实姓名
     */
    private String checkName;

    /**
     * 用户客户端IP
     */
    private String clientIp;

    /**
     * 外部订单号
     */
    private String outTradeNo;

    /**
     * 外部协议编号
     */
    private String outAgrNo;

    /**
     * 审核人员id
     */
    private Integer auditorId;

    /**
     * 审核人员
     */
    private String auditor;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 支付终端系统
     * IOS, ANDROI, WEB, H5, WX-APPLET, WX-PUBLIC
     */
    private String sysCnl;

    /**
     * 交易类型
     * JSAPI:公众号或小程序支付,
     * APP:app支付,
     * NATIVE:扫码支付,
     * MICROPAY:刷卡支付,
     * MWEB:H5支付
     */
    private String tradeType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 返回码
     */
    private String returnCode;

    /**
     * 返回信息
     */
    private String returnMsg;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;
}
