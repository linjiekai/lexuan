package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信首次登录绑定DTO
 */
@Data
public class UserWXBindDTO implements Serializable {
    private static final long serialVersionUID = 8112700353098853015L;
    private String mobile;
    private String code;
    private String wxOpenId;
    private String icon;
    private String name;
    private String gender;
    private Integer sureBind;//0：返回提示、1 : 确定绑定
    private String tokenId;
    private String areaCode;// 区域编号
    private Integer isOwnMobile;// 小程序，0：当前手机号并非是原来的微信提供的，1：当前手机号是微信提供的
    private String inviteCode;// 邀请码
    private String platform;// 平台
}
