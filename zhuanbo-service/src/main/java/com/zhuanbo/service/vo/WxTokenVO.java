package com.zhuanbo.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WxTokenVO implements Serializable{

	private static final long serialVersionUID = -8763852179723237489L;

	private String tokenId;
	
	private String mobile;
	
	private String code;
	
	@JsonProperty("appid")
	private String appId;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String secret;
	
	@JsonProperty("access_token")
	private String accessToken;
	
	@JsonProperty("refresh_token")
	private String refreshToken;
	
	@JsonProperty("openid")
	private String openId;
	
	@JsonProperty("expires_in")
	private String expiresIn;
	
	private String scope;
	
	private String unionid;
	
	private String nickName;
	
	private Integer sex;
	
	private String headimgUrl;
	
	private String errcode;
	
	private String errmsg;

	/**
	 * code来源：wx:微信，mp:小程序
	 */
	private String source;

	private String sessionKey;
}
