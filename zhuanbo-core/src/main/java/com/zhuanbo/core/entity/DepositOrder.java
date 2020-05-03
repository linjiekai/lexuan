package com.zhuanbo.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * <p>
 * 充值订单表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_deposit_order")
@Data
public class DepositOrder implements Serializable {

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
     * 充值订单号
     */
    private String depositNo;

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
     * 订单类型 1:VIP499 2:赚播店长2500 3:赚播总监10000 4:赚播合伙人30000 5:赚播联创90000
     */
    private Integer orderType;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 联系人手机号
     */
    private String mobile;

    /**
     * 商城会员ID
     */
    private Long userId;

    /**
     * 订单金额
     */
    private BigDecimal price;
    
    private BigDecimal reducePrice;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品支付：MPPAY
     */
    private String bankCode;

    /**
     * 业务类型 01：充值;04：收益; 05：押金; 06：会员充值 ; 07：扣减
     */
    private String busiType;
    
    /**
     * 订单类型拆分
     */
    private List<Integer> typeSplit;
    
    /**
     * 升级等级
     */
    private Integer upgradeLevel;
    
    /**
     * 交易编号 01充值 02消费 08调账
     */
    private String tradeCode;

    /**
     * 状态 待充值:W;充值成功:S;充值确认:BW;充值失败:F;
     */
    private String orderStatus;

    /**
     * 支付订单号
     */
    private String payNo;

    /**
     * 支付日期yyyy-MM-dd
     */
    private String payDate;

    /**
     * 支付时间HH:mm:ss
     */
    private String payTime;

    /**
     * 邀请码
     */
    private String inviteCode;
    
    /**
     * 购买码
     */
    private String buyCode;
    
    /**
     * 赠送码 buyType=2必填
     */
    private String couponSn;
    
    /**
     * 购买类型 0：普通购买 1：分享购买 2:赠品
     */
    private Integer buyType;

    /**
     * 客户端IP
     */
    private String clientIp;

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
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;
    
    /**
     * 源订单号
     */
    @TableField(exist = false)
    private String sourceOrderNo;

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
}
