package com.zhuanbo.service.service;


import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.service.vo.WxTokenVO;

public interface IWxApiService {

	public WxTokenVO thirdLogin(WxTokenVO wxTokenVO, String requestUrl);
	
	public WxTokenVO userinfo(WxTokenVO wxTokenVO, String requestUrl);

	/**
	 * 获取小程序session_key和openid
	 * @param code
	 * @param appId
	 * @param secret
	 * @param requestUrl
	 * @return
	 */
	JSONObject code2Session(String code, String appId, String secret, String requestUrl);
}
