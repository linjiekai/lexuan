package com.zhuanbo.admin.api.dto.withdrorder;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @title: WithdrOrderDTO
 * @projectName mpmall.api
 * @description: 体现订单
 * @date 2019/10/23 22:31
 */
@Data
public class WithdrOrderDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 商城订单号
     */
    private String orderNo;

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
     * 开户行银行联行号
     */
    private String bankNo;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 银行名称：支付宝 微信 名品猫 平安银行 ...
     */
    private String bankName;

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
     * 用户客户端IP
     */
    private String clientIp;

    /**
     * 审核人员id
     */
    private String auditorId;

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
     * 创建时间
     */
    private LocalDateTime addTime;
}
