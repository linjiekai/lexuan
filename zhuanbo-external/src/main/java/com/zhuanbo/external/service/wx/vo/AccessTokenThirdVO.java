package com.zhuanbo.external.service.wx.vo;

import lombok.Data;

@Data
public class AccessTokenThirdVO {
    private String accessToken;
    private  String expiresIn;
    private String refreshToken;
    private String openId;
    private String scope;
    private String unionid;
}
