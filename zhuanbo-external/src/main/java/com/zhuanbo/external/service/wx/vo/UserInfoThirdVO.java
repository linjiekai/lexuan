package com.zhuanbo.external.service.wx.vo;

import lombok.Data;

/**
 * 第三方平台用户信息
 *
 */
@Data
public class UserInfoThirdVO {

    private String nickName;
    private String headimgUrl;
    private String unionid;
    private String sex;
    private String sessionKey;
    private String openId;
}
