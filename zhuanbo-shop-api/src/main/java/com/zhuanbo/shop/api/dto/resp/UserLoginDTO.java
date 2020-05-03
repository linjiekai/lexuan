package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回数据DTO
 */
@Data
public class UserLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String accid;
    private String userName;
    private String headImgUrl;
    private String nickname;
    private String name;
    private Integer gender;
    private String mobile;
    private String telPhone;
    private String email;
    private String birthday;
    private String userToken;
    private String token;
    private String openId;
    private Integer hasPwd = 0;// 是否设置过密码。0：未设置，1：已设置
    private String areaCode;
    private String wxName;// 微信昵称
    private String wxOpenId;
    private Integer ptLevel;
    private String ptNo;
    private String inviteCode;
    private Integer ptFormal;
    private Integer realed;
    private Integer cardType;
    private String cardNoAbbr;
    private String authNo;
    private String authDate;
}
