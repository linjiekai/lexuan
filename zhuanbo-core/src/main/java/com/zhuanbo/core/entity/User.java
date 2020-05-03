package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户基础信息表
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_user")
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    /**
     * 用户登录名
     */
    @NotBlank(message="名称不能为空")
    private String userName;

    /**
     * 用户登录密码
     */

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
     * 证件号简称
     */
    private String cardNoAbbr;

    /**
     * 状态 0:待审核 1:正常 2:冻结/黑名单
     */
    private Integer status;

    /**
     * 合伙人等级  [0:普通用户, 1:VIP, 2:店长, 3:总监, 4:合伙人, 5:联创]
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
     * 实名认证标记 0：未实名 1：弱实名 2：强实名
     */
    private Integer realed;

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
     * 操作人
     */
    private String operator;

    /**
     * 屏蔽。0：未屏蔽、1：已屏蔽
     */
    private Integer shield;

    /**
     * 是否进行签名 [0:否, 1:是]
     */
    private Integer signed;

    /**
     * 签名图片url地址
     */
    private String signImgUrl;

    /**
     * 签名时间
     */
    private LocalDateTime signTime;
    
    private Long liveUserId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 用户上级
     */
    @TableField(exist = false)
    private Long inviteUpUserId;
}
