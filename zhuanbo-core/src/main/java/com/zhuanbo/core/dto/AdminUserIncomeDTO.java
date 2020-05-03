package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 * @title: AdminUserIncomeDTO
 * @description: 用户收益DTO
 * @date 2020/4/23 17:23
 */
@Data
public class AdminUserIncomeDTO implements Serializable {

    /**
     * 用户昵称
     */
    private Long userId;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户等级
     */
    private Integer ptLevel;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 用户剩余积分
     */
    private Integer usablePoint;
    /**
     * 用户剩余积分
     */
    private String remark;

}
