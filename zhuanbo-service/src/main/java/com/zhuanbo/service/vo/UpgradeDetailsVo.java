package com.zhuanbo.service.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpgradeDetailsVo {

    private Long id;

    /**
     * 用户id
     */
    private  Long userId;
    /**
     * 用户电话号码
     */
    private String mobile;

    /**
     * 用户等级
     */
    private Integer ptLevel;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 是否退款 0：否 1：是
     */
    private Integer refundFlag;

    /**
     * 支付日期yyyy-MM-dd
     */
    private String payDate;

    /**
     * 支付时间HH:mm:ss
     */
    private String payTime;

    /**
     * 直属达人数
     */
    private Integer darenNum;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 支付方式 0：线下 1：线上
     */
    private Integer payType;




}
