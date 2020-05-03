package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserDTO extends AdminBaseRequestDTO {
    /**
     * 主键ID
     */
    private Long id;


    /**
     * 用户登录名
     */
    private String userName;

    /**
     * 用户登录密码
     */
    private String password;

    /**
     * 盐值
     */
    private String salt;

    /**
     * 头像url地址
     */
    private String headImgUrl;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 0:未知 1:男 2:女
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 电话
     */
    private String telPhone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 生日 yyyy-MM-dd
     */
    private String birthday;

    /**
     * 行政区域表的省ID
     */
    private Integer provinceId;

    /**
     * 行政区域表的市ID
     */
    private Integer cityId;

    /**
     * 行政区域表的区县ID
     */
    private Integer areaId;

    /**
     * 行政区域表的乡镇ID
     */
    private Integer countryId;

    /**
     * 地址
     */
    private String address;

    /**
     * 邮编号码
     */
    private String zipCode;

    /**
     * 证件类型 0:身份证 1:护照 9:其它
     */
    private Integer cardType;

    /**
     * 证件号
     */
    private String cardNo;

    /**
     * 状态 0:待审核 1:正常 2:冻结/黑名单
     */
    private Integer status;

    /**
     * 合伙人等级 0:M星人;1:M体验官;2:M达人;3:M司令
     */
    private Integer ptLevel;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 合伙人编号
     */
    private String ptNo;

    /**
     * 是否正式 0：实习 1：正式
     */
    private Integer ptFormal;

    /**
     * 最后登陆IP
     */
    private String lastLoginIp;

    /**
     * 最后一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 注册日期yyyy-MM-dd
     */
    private String regDate;

    /**
     * 注册时间HH:mm:ss
     */
    private String regTime;

    /**
     * 乐观锁字段
     */
    private Integer version;

    /**
     * 区域编号
     */
    private String areaCode;

    /**
     * 是否删除。0：否，1：是
     */
    private Integer deleted;

    /**
     * 屏蔽。0：未屏蔽、1：已屏蔽
     */
    private Integer shield;

    /**
     *  实名。0：未实名、1：已实名
     */
    private Integer realed;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    private LocalDateTime updateTime;

    /**
     * 0：屏蔽、1.解除屏蔽，2 封禁，3.解除封禁
     */
    private Integer operateType;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 管理平台创建用户 - 积分
     */
    private Integer point;

    /**
     * 邀请码
     */
    private String authNo;

    /**
     * 支付方式 [1:积分方式]
     */
    private Integer payType;

    /**
     * 备注
     */
    private String remark;

}
