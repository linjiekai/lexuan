package com.zhuanbo.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 验证
 */
@Component
@ConfigurationProperties(prefix = "auth")
@Data
public class AuthConfig {

    private Long codeTime;// 验证码有效期,单位：秒
    private Long loginTokenTime;// 登录token有效时间，单位：天
    private Map<String, Object> sign;// 接口验签


    private String chinaCode="86";
    private String accessKeyName;// 阿里短信签名
    private String accessKeyId;// 阿里短信AccessKeyId
    private String accesskeySecret;// 阿里短信AccessKeySecret
    private String smsTemplateBind;// 阿里短信-绑定模板
    private String smsTemplateLogin;// 阿里短信-登录模板
    private String smsTemplateRegister;// 阿里短信-注册模板
    private String smsTemplateResetPwd;// 阿里短信-重置(忘记)密码模板
    private String smsTemplateBindOverSea;// 阿里短信-绑定模板
    private String smsTemplateLoginOverSea;// 阿里短信-登录模板
    private String smsTemplateRegisterOverSea;// 阿里短信-注册模板
    private String smsTemplateResetPwdOverSea;// 阿里短信-重置(忘记)密码模板
    private String aliOpenApiUrl;//阿里开放api接口URL
    private String aliAppId;    //阿里应用appid
    private String aliPrivateKey;    //阿里应用私钥
    private String aliPublicKey;    //阿里应用公钥

    private String wxAppId;// 微信AppID
    private String wxSecret;// 微信应用密钥AppSecret
    private String wxAppIdWj;// 微信AppID(玩家)
    private String wxSecretWj;// 微信应用密钥AppSecret(玩家)
    private String wxMpAppId;// 微信AppID(小程序)
    private String wxMpSecret;// 微信应用密钥AppSecret（小程序）
    private String wxGzhAppId;// 微信AppID
    private String wxGzhSecret;// 微信应用密钥AppSecret
    private String wxOauth2AccessTokenUrl;//微信登陆AccessToken接口URL
    private String wxAccessTokenUrl;// 微信全局AccessToken接口URL
    private String wxUserinfoUrl;// 微信获取用户个人信息接口URL
    private String mpSessionUrl;// 小程序获取用Session的URL
    private String wxTicketUrl;

    private String shipCompanyUrl;// 快递公司
    private String shipTraceUrl;// 快递物流跟踪
    private String shipAppCode;// 快递appCode
    
    private String payUrl;// 支付url地址
    private String mercId;// 商户号
    private String mercPrivateKey;// 密钥
    private String notifyUrl;
    private String payUrlIp;// 支付url地址ip
    private String goodShareUrl;//分享url
    private String dynamicShareUrl;
    private String goodIconStyle;

    private String getliveinfo;
    
    private String liveUrl;
    private String mliveApiUrl;    // php赚播地址
    private String mliveAdminUrl;    // php赚播admin地址
    private String mliveUrl;    // php赚播golang地址

    private String stockCutUrl;// 扣云仓库存
    private String stockCutRollbackUrl;// 回滚扣云仓库存
    private String mliveSignKey;// 签名key
    private String localUrl;// 当前服务器访问地址

    private Integer goodsNumber; // 商品购买数量限制
    private Integer shipGoodsNumber; // 计算运费：商品购买数量限制

    private String h5appid;
    private String h5secret;
    private String phpUserUrl;
    private String phpCloudRefundUrl;
    private Integer orderCancelTimes; //订单取消间隔时间

    private String phpTeamInSizeUrl;// 团队内校验
}
