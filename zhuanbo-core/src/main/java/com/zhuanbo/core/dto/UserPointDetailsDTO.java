package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Administrator
 * @title: UserPointDetailsDTO
 * @description: 用户积分明细
 * @date 2020/4/23 19:36
 */
@Data
public class UserPointDetailsDTO extends BaseDTO implements Serializable {

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
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 操作积分 本次操作的积分值
     */
    private Integer operatePoint;

    /**
     * 可用积分
     */
    private Integer usablePoint;

    /**
     * 操作类型 1：增加积分 2：减少积分
     */
    private Integer operateType;

    /**
     * 业务类型 1：充值 2：支付扣减
     */
    private Integer pointType;

    /**
     * 积分状态 1：有效 2：已扣除(扣完) 3：已过期 4：冻结中 5：冻结返还 6：冻结扣减
     */
    private Integer status;

    /**
     * 登记日期yyyy-MM-dd
     */
    private String pointDate;

    /**
     * 登记时间HH:mm:ss
     */
    private String pointTime;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long fromUserId;

    private Long fromUserMobile;

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

    private String nickname;

    private String mobile;

    private String authNo;

    private Long operatorId;
    private String operator;

    private Integer days;
}
