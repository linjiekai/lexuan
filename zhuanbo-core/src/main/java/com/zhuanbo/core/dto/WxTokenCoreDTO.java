package com.zhuanbo.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WxTokenCoreDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8763852179723237489L;
	private String tokenId;
	private String mobile;
	private String code;
	@JsonProperty("appid")
	private String appId;
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
	private String nickname;
	private Integer sex;
	private String headimgurl;
	private String errcode;
	private String errmsg;
	private String source;// 微信绑定来源。微信：wx、小程序：mp
	private String encryptedData;//
	private String iv;
	private String platform = "zhuanbo";
}
