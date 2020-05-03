package com.zhuanbo.core.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Administrator
 * @title: AdminCheckUserDTO
 * @description: 用户和邀请码检测
 * @date 2020/4/23 15:00
 */
@Data
public class AdminCheckUserDTO {

    /**
     * 代理类型, 1:面膜业务
     */
    private Integer proxyType;

    /**
     * 用户等级
     */
    private Integer ptLevel;

    /**
     * 用户等级列表
     */
    private List<Integer> ptLevels;

    /**
     * 商品类型 [1:面膜499套餐]
     */
    private Integer goodsType;

    /**
     * 商品模型 [1:面膜]
     */
    private Integer goodsModel;

    /**
     * 支付方式 [1:积分支付]
     */
    private Integer payType;

    /**
     * 支付金额/积分
     */
    private BigDecimal price;

    /**
     * 支付金额/积分
     */
    private Integer point;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否可修改邀请上级
     */
    private Integer modifyHigherUps;
    /**
     * 是否可修改等级
     */
    private Integer modifyPtLevel;
    /**
     * 是否可修改商品类型
     */
    private Integer modifyGoodsType;
    /**
     * 是否是新用户
     */
    private Integer isNewUser;

}
