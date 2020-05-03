package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录参数DTO
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1531688389423525737L;

    private Integer type;// 2：密码登陆、3：微信登陆、4：验证码登录 、
    private String mobile;// 手机号(微信登陆不必填)
    private String password;// 密码（密码登陆）
    private String code;// 验证码（验证码登陆）
    private String openId;// 微信openid（微信登陆）
    private String icon;// 微信用户头像（微信登陆）
    private String name;// 微信昵称（微信登陆）
    private Integer gender;// 微信性别，0表示未知，1表示男，2女表示女（微信登陆）
    private String platform = "zhuanbo";
}
