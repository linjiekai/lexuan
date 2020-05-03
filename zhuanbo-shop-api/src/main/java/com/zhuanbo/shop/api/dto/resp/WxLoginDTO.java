package com.zhuanbo.shop.api.dto.resp;

import com.zhuanbo.core.entity.User;

public class WxLoginDTO {
    private String code;
    private User userInfo;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }
}
