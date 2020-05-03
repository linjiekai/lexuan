package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ALLRegisterDTO implements Serializable {

    private static final long serialVersionUID = 7359677435856674621L;
    private Integer type;
    private String areaCode;// 国家区域编号
    private String mobile;
    private String code;// 验证码
    private String source;// 当前注册方法来源。api:接口、method:内部方法
    private String inviteCode;// 邀请码
    private Integer withoutCode = 1;// 0:不要验证码登录，1:要验证码登录
    private String wxOpenId;
    private String icon;
    private String name;
    private String gender;
    private Integer sureBind;//0：返回提示、1 : 确定绑定
    private String tokenId;
    private Integer isOwnMobile;// 小程序，0：当前手机号并非是原来的微信提供的，1：当前手机号是微信提供的
    private String platform = "ZBMALL";// 平台
}
