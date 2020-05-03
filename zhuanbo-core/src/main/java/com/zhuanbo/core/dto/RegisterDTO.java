package com.zhuanbo.core.dto;

import com.zhuanbo.core.constants.ConstantsEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterDTO implements Serializable {
    private static final long serialVersionUID = 1364971087371183101L;

    private String mobile;
    private String wxOpenId;
    private String icon;
    private String name;
    private String gender;
    private Integer sureBind;//0：返回提示、1 : 确定绑定
    private String tokenId;
    private String areaCode;// 区域编号
    private Integer isOwnMobile;// 小程序，0：当前手机号并非是原来的微信提供的，1：当前手机号是微信提供的
    private String inviteCode;// 邀请码
    private String platform = "ZBMALL";// 平台
    private Integer type;
    private String code;// 验证码
    private String source;// 当前注册方法来源。api:接口、method:内部方法
    private Integer withoutCode = 1;// 0:不要验证码登录，1:要验证码登录
    private String javaBindType = ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.stringValue();
    private String mpCode;// 小程序的code
    private String encryptedData;
    private String iv;
    private String sysCnl;// IOS、ANDROID、H5、WEB、WX-APPLET、WX-PUBLIC
    private Integer needUnionid = 0;
    private Integer needBindUnionid = 0;// 是否绑定unionid,0:不要，1：要，默认不要，0
    private String unionidStr;
    private String sessionKeyStr;// sessionKeyStr -> sessionKey
}
