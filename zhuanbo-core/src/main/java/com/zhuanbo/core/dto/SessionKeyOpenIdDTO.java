package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class SessionKeyOpenIdDTO {
    private String session_key;
    private String openid;
    private String unionid;
    private String openId;
    private String nickName;
    private String avatarUrl;
    private String sessionKeyStr;
}
