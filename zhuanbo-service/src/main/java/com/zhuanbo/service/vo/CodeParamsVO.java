package com.zhuanbo.service.vo;

import lombok.Data;

@Data
public class CodeParamsVO {
    private Integer type;
    private String areaCode;// 国家区域编号
    private String mobile;
    private String code;// 验证码
    private String source;// 当前注册方法来源。api:接口、method:内部方法
    private String inviteCode;// 邀请码
    private String platform;// 平台
    private Integer withoutCode = 1;// 0:不要验证码登录，1:要验证码登录
}
