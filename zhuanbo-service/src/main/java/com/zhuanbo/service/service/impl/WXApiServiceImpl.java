package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.service.service.IWxApiService;
import com.zhuanbo.service.vo.WxTokenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Administrator
 *
 */
@Service
@Slf4j
public class WXApiServiceImpl implements IWxApiService {

	public WxTokenVO thirdLogin(WxTokenVO wxTokenVO, String requestUrl) {

		StringBuffer strBuff = new StringBuffer();
		//appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
		strBuff.append("appid=").append(wxTokenVO.getAppId());
		strBuff.append("&secret=").append(wxTokenVO.getSecret());
		strBuff.append("&code=").append(wxTokenVO.getCode());
		strBuff.append("&grant_type=authorization_code");
		log.info("获取微信accessToken param:{}", strBuff.toString());
		String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
		log.info("thirdLogin result:{}", result);
		WxTokenVO token = JSON.parseObject(result, WxTokenVO.class);
		token.setTokenId(wxTokenVO.getTokenId());
		return token;
	}

	public WxTokenVO userinfo(WxTokenVO wxTokenDTO, String requestUrl) {
		StringBuffer strBuff = new StringBuffer();
		//appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
		strBuff.append("access_token=").append(wxTokenDTO.getAccessToken());
		strBuff.append("&openid=").append(wxTokenDTO.getOpenId());
		log.info("获取微信用户个人信息param:{}", strBuff.toString());
		String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
		log.info("userinfo result:{}", result);
		WxTokenVO token = JSON.parseObject(result, WxTokenVO.class);
		token.setTokenId(wxTokenDTO.getTokenId());
		return token;
	}

	@Override
	public JSONObject code2Session(String code, String appId, String secret, String requestUrl) {

		StringBuffer strBuff = new StringBuffer();
		//appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code
		strBuff.append("appid=").append(appId);
		strBuff.append("&secret=").append(secret);
		strBuff.append("&js_code=").append(code);
		strBuff.append("&grant_type=").append("authorization_code");
		log.info("获取小程序session_keyparam:{}", strBuff.toString());
		String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
		log.info("result:{}", result);
		return JSON.parseObject(result);
	}

}
