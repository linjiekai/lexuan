package com.zhuanbo.external.service.wx.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.core.util.aliPay.AlipayConstants;
import com.zhuanbo.core.util.aliPay.AlipaySignature;
import com.zhuanbo.core.util.aliPay.WebUtils;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.key.IKeyService;
import com.zhuanbo.external.service.key.impl.KeyService;
import com.zhuanbo.external.service.wx.service.IThirdService;
import com.zhuanbo.external.service.wx.vo.AccessTokenThirdVO;
import com.zhuanbo.external.service.wx.vo.ShareTicketThirdVO;
import com.zhuanbo.external.service.wx.vo.TicketVO;
import com.zhuanbo.external.service.wx.vo.UserInfoThirdVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service("alipayThirdService")
@Slf4j
public class IAlipayThirdServiceImpl implements IThirdService {

	@Autowired
    private AuthConfig authConfig;
    
    @Autowired
    private IKeyService iKeyService;

    /**
     * 用户登录
     *
     * @param code
     * @param encryptedData
     * @param iv
     * @return
     */
    @Override
    public UserInfoThirdVO mpLogin(AppIdKeyDTO appIdKeyDTO, String code, String encryptedData, String iv) throws Exception {
        return null;
    }

    /**
     * 获取 支付宝用户AccessToken
     *
     * @param code
     * @return
     */
    @Override
    public AccessTokenThirdVO getAccessTokenVO(AppIdKeyDTO appIdKeyDTO, String code, String platform) throws Exception {
    	
    	Map<String, Object>  keyMap = iKeyService.param(appIdKeyDTO);

        String appId = String.valueOf(keyMap.get(KeyService.BACK_APP_ID));
        String privateKey = String.valueOf(keyMap.get(KeyService.PRIVATE_KEY));
        String publicKey = String.valueOf(keyMap.get(KeyService.PUBLIC_KEY));
        
        Map<String, Object> data = buildPublicParams(appId, AlipayConstants.METHOD_SYSTEM_IOAUTH_TOKENE);
        
        data.put(AlipayConstants.GRANT_TYPE, AlipayConstants.AUTHORIZATION_CODE);
        data.put(AlipayConstants.CODE, code);

        String plain = Sign.getPlain(data, true);
		String sign = AlipaySignature.rsa256Sign(plain, privateKey, AlipayConstants.CHARSET_UTF8);
		data.put("sign", sign);

        String paramStr = WebUtils.buildQuery(data, AlipayConstants.CHARSET_UTF8);
        String url = authConfig.getAliOpenApiUrl() + "?" + paramStr;
        log.info("获取支付宝 accessToken request url :{}",url);
        log.info("获取支付宝 accessToken request  params:{}",JacksonUtil.objTojson(data));
        String response = HttpUtil.httpsRequest(url, "POST", null);
        log.info("获取支付宝 accessToken response:{}", response);
        //解析结果
        Map<String, Object> resultMap = verify(publicKey, response, "alipay_system_oauth_token_response");
        AccessTokenThirdVO vo = new AccessTokenThirdVO();
        vo.setOpenId(String.valueOf(resultMap.get("user_id")));
        vo.setAccessToken(String.valueOf(resultMap.get("access_token")));
        vo.setExpiresIn(String.valueOf(resultMap.get("expires_in")));
        vo.setRefreshToken(String.valueOf(resultMap.get("refresh_token")));
        return vo;

    }


    /**
     * 获取 全局accessToken
     *
     * @return
     */
    @Override
    public String getOverAllAccessToken(AppIdKeyDTO appIdKeyDTO, boolean delCache) {
        return null;
    }


    /**
     * 获取支付宝用户信息
     *
     * @param accessToken
     * @param openId
     * @return
     */
    @Override
    public UserInfoThirdVO getUserInfo(AppIdKeyDTO appIdKeyDTO, String accessToken, String openId) throws Exception {
    	
    	Map<String, Object>  keyMap = iKeyService.param(appIdKeyDTO);

        String appId = String.valueOf(keyMap.get(KeyService.BACK_APP_ID));
        String privateKey = String.valueOf(keyMap.get(KeyService.PRIVATE_KEY));
        String publicKey = String.valueOf(keyMap.get(KeyService.PUBLIC_KEY));
        
        
        Map<String, Object> map = buildPublicParams(appId, AlipayConstants.METHOD_USER_INFO_SHARE);
        map.put(AlipayConstants.ACCESS_TOKEN, accessToken);
        
        String plain = Sign.getPlain(map,true);
        String sign = AlipaySignature.rsa256Sign(plain, privateKey, AlipayConstants.CHARSET_UTF8);
        map.put(AlipayConstants.SIGN, sign);

        String paramStr = WebUtils.buildQuery(map, AlipayConstants.CHARSET_UTF8);
        String url = authConfig.getAliOpenApiUrl() + "?" + paramStr;
        log.info("获取支付宝  用户信息 request url: {}",url);
        log.info("获取支付宝  用户信息 request params: {}", JacksonUtil.objTojson(map));
        String response = HttpUtil.httpsRequest(url, "POST", null);
        log.info("获取支付宝 用户信息 response:{}", response);
        //解析结果
        Map<String, Object> resultMap = verify(publicKey, response, "alipay_user_info_share_response");

        UserInfoThirdVO vo = new UserInfoThirdVO();
        vo.setOpenId(String.valueOf(resultMap.get("user_id")));
        vo.setSex(String.valueOf(resultMap.get("gender")));
        vo.setHeadimgUrl(String.valueOf(resultMap.get("avatar")));
        vo.setNickName(String.valueOf(resultMap.get("nick_name")));
        return vo;
    }

    /**
     * 获取ticket
     *
     * @return
     */
    @Override
    public ShareTicketThirdVO getShareTicketVO(AppIdKeyDTO appIdKeyDTO,String url) {
        return null;
    }

    @Override
    public String code2openid(AppIdKeyDTO appIdKeyDTO, String code) throws Exception {
        AccessTokenThirdVO accessTokenVO = getAccessTokenVO(appIdKeyDTO, code, null);
        return accessTokenVO.getOpenId();
    }


    public Map<String, Object> buildPublicParams(String appId, String method) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(AlipayConstants.APP_ID, appId);
        data.put(AlipayConstants.CHARSET, AlipayConstants.CHARSET_UTF8);
        data.put(AlipayConstants.SIGN_TYPE, AlipayConstants.SIGN_TYPE_RSA2);
        data.put(AlipayConstants.TIMESTAMP, LocalDateTime.now().format(DateTimeFormatter.ofPattern(AlipayConstants.DATE_TIME_FORMAT)));
        data.put(AlipayConstants.VERSION, AlipayConstants.VERSION_1);
        data.put(AlipayConstants.METHOD, method);
        data.put(AlipayConstants.FORMAT, AlipayConstants.FORMAT_JSON);
        return data;
    }

    private Map<String, Object> verify(String publicKey, String jsonStr, String nodeName) throws Exception {
        Map<String, Object> resultMap = (Map<String, Object>) JSONObject.parseObject(jsonStr, Map.class);
        Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get(nodeName) != null ? (Map<String, Object>) resultMap.get(nodeName) : (Map<String, Object>) resultMap.get("error_response");

        if (null != bodyMap.get("code") && !AlipayConstants.SUCCESS_10000.equals(bodyMap.get("code"))) {
            throw new ShopException("11203", String.valueOf(bodyMap.get("sub_msg")));
        }
        String sign = String.valueOf(resultMap.get("sign"));
        int signDataStartIndex = jsonStr.indexOf(nodeName) + nodeName.length() + 2;
        String content = AlipaySignature.extractSignContent(jsonStr, signDataStartIndex);
        boolean verify = AlipaySignature.rsa256CheckContent(content, sign, publicKey, AlipayConstants.CHARSET_UTF8);
        if (!verify) {
            log.error("支付宝返回报文验签失败,待签名串：{},支付宝返回签名串：{}", content, sign);
            throw new ShopException(11202);
        }
        return bodyMap;
    }

	@Override
	public TicketVO getTicket(String url, String accessToken) {
		return null;
	}
}
