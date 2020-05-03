package com.zhuanbo.core.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @title: AdminPointDTO
 * @description: 用户积分
 * @date 2020/4/22 22:44
 */
@Data
public class AdminPointDTO extends BaseDTO{

    /**
     * 用户
     */
    private Long userId;
    /**
     * 手机区号
     */
    private String areaCode;
    /**
     * 授权号
     */
    private String authNo;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 被邀请用户
     */
    private Long fromUserId;
    /**
     * 积分
     */
    private Integer point;
    /**
     * 积分
     */
    private Integer price;
    /**
     * 业务类型 [1:充值, 2:支付扣减(兑换商品)]
     */
    private Integer pointType;
    /**
     * 操作类型 [1:增加积分, 2:减少积分]
     */
    private Integer operateType;
    /**
     * 交易编号 01充值 02消费 08调账
     */
    private String tradeCode;
    /**
     * 业务类型 01：充值;04：收益; 05：押金 06：会员套餐, 07:扣减
     */
    private String busiType;
    /**
     * 购买类型 [0:普通购买, 1:分享购买, 2:赠品, 3:积分]
     */
    private Integer buyType;
    /**
     * 类型拆分
     */
    private List<Integer> typeSplit;
    /**
     * 操作人
     */
    private Long adminId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户等级
     */
    private Integer ptLevel;

    /**
     * 充值订单号
     */
    private String depositNo;

    /**
     * 备注
     */
    private String remark;
}
