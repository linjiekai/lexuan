package com.zhuanbo.external.service.wx.service.impl;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.crypt.WxMaCryptUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.MapKeyEnum;
import com.zhuanbo.core.dto.MobileMPMobileDTO;
import com.zhuanbo.core.dto.SessionKeyOpenIdDTO;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.CheckSumBuilder;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.key.IKeyService;
import com.zhuanbo.external.service.key.impl.KeyService;
import com.zhuanbo.external.service.wx.service.IThirdService;
import com.zhuanbo.external.service.wx.service.IWeixinIndependenceService;
import com.zhuanbo.external.service.wx.vo.AccessTokenThirdVO;
import com.zhuanbo.external.service.wx.vo.ShareTicketThirdVO;
import com.zhuanbo.external.service.wx.vo.TicketVO;
import com.zhuanbo.external.service.wx.vo.UserInfoThirdVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service("weixinThirdService")
@Slf4j
public class IWeixinThirdServiceImpl implements IThirdService, IWeixinIndependenceService {

    public static final String ZHUANBOSHOP_MP_CODE = "zhuanboshop:mp:code:";
    private final String PLATFORM_MPMALL = "ZBMALL";
    private final String LOCK_ACCESS_TOKEN = "zhuanbo:accessToken:lock:key";
    public static final String MP_MPAMLL_ACCESSTOKEN = "mp:zhuanbo:accesstoken";
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private RedissonDistributedLocker redissonDistributedLocker;
    @Autowired
    private IKeyService iKeyService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 小程序登陆
     *
     * @param code
     * @param encryptedData
     * @param iv
     * @return
     */
    @Override
    public UserInfoThirdVO mpLogin(AppIdKeyDTO appIdKeyDTO, String code, String encryptedData, String iv) {

    	Map<String, Object> keyMap = iKeyService.param(appIdKeyDTO);

        String appId = String.valueOf(keyMap.get(KeyService.BACK_APP_ID));
        String secret = String.valueOf(keyMap.get(KeyService.BACK_SECRECT));
        String requestUrl = authConfig.getMpSessionUrl();
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("appid=").append(appId);
        strBuff.append("&secret=").append(secret);
        strBuff.append("&js_code=").append(code);
        strBuff.append("&grant_type=authorization_code");
        log.info("获取小程序session_keyparam:{}", strBuff.toString());
        String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
        log.info("result:{}", result);

        JSONObject sessionJSON = JSON.parseObject(result);
        if (StringUtils.isNotBlank(sessionJSON.getString("errcode"))) {
            log.error("小程序code获取session失败:{}", sessionJSON);
            return null;
        }
        UserInfoThirdVO wxUserInfoVO = new UserInfoThirdVO();
        wxUserInfoVO.setSessionKey(sessionJSON.getString("session_key"));
        wxUserInfoVO.setOpenId(sessionJSON.getString("openid"));
        wxUserInfoVO.setUnionid(sessionJSON.getString("unionid"));
        return wxUserInfoVO;
    }

    /**
     * app 普通登陆获取accessTokenVo
     *
     * @param code
     * @return
     */
    @Override
    public AccessTokenThirdVO getAccessTokenVO(AppIdKeyDTO appIdKeyDTO, String code, String platform) {

        Map<String, Object>  keyMap = iKeyService.param(appIdKeyDTO);

        String appId = String.valueOf(keyMap.get(KeyService.BACK_APP_ID));
        String secret = String.valueOf(keyMap.get(KeyService.BACK_SECRECT));
        String requestUrl = authConfig.getWxOauth2AccessTokenUrl();
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("appid=").append(appId);
        strBuff.append("&secret=").append(secret);
        strBuff.append("&code=").append(code);
        strBuff.append("&grant_type=authorization_code");
        log.info("获取微信accessToken param:{}", strBuff.toString());

        String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
        log.info("result:{}", result);
        JSONObject jsonObject = JSON.parseObject(result);
        AccessTokenThirdVO wxAccessTokenVO = new AccessTokenThirdVO();
        wxAccessTokenVO.setOpenId(jsonObject.getString("openid"));
        wxAccessTokenVO.setAccessToken(jsonObject.getString("access_token"));
        wxAccessTokenVO.setExpiresIn(jsonObject.getString("expires_in"));
        wxAccessTokenVO.setRefreshToken(jsonObject.getString("refresh_token"));
        wxAccessTokenVO.setScope(jsonObject.getString("scope"));
        wxAccessTokenVO.setUnionid(jsonObject.getString("unionid"));

        // h5, openid找unionid
        if (StringUtils.isBlank(wxAccessTokenVO.getUnionid()) && (ConstantsEnum.USER_BIND_THIRD_TYPE_H5.stringValue().equalsIgnoreCase(appIdKeyDTO.getSource())
        		|| ConstantsEnum.USER_BIND_THIRD_TYPE_WX_PUBLIC.stringValue().equalsIgnoreCase(appIdKeyDTO.getSysCnl()))
        		) {
            StringBuffer unionidParams = new StringBuffer();
            try {
                unionidParams.append("access_token=").append(jsonObject.getString("access_token"));
                unionidParams.append("&openid=").append(jsonObject.getString("openid"));
                unionidParams.append("&lang=zh_CN");

                log.info("获取H5_Unionid参数|{}", unionidParams.toString());
                String unionidResult = HttpUtil.sendGet(authConfig.getWxUserinfoUrl(), unionidParams.toString());
                log.info("获取H5_Unionid结果|{}", unionidResult);

                JSONObject unionidJSON = JSON.parseObject(result);
                if (StringUtils.isBlank(unionidJSON.getString("errcode"))) {
                    wxAccessTokenVO.setUnionid(unionidJSON.getString("unionid"));
                }
            } catch (Exception e) {
                log.error("获取H5_Unionid失败|{}|{}", unionidParams,e);
            }
        }
        return wxAccessTokenVO;
    }


    /**
     * 公众号获取accessToken
     *
     * @return
     */
    @Override
    public String getOverAllAccessToken(AppIdKeyDTO appIdKeyDTO, boolean delCache) {

        Map<String, Object> keyMap = iKeyService.param(appIdKeyDTO);
        String appId = String.valueOf(keyMap.get(KeyService.BACK_APP_ID));
        String secret = String.valueOf(keyMap.get(KeyService.BACK_SECRECT));
        if (delCache) {
            RedisUtil.del(appId);
        }
        //获取accessToken 如果不为空直接获取用户信息返回
        Object accessToken = RedisUtil.get(appId);
        log.info("请求微信accessToken旧的accessToken{},{}", accessToken, (accessToken == null));
        if (accessToken == null) {
            // 加锁
            boolean tryLock = redissonDistributedLocker.tryLock(LOCK_ACCESS_TOKEN, TimeUnit.SECONDS, 60, 60);
            if (tryLock) {
                accessToken = RedisUtil.get(appId);
                if (accessToken != null) {
                    redissonDistributedLocker.unlock(LOCK_ACCESS_TOKEN);
                    return String.valueOf(accessToken);
                }

                try {
                    String requestUrl = authConfig.getWxAccessTokenUrl();
                    StringBuffer strBuff = new StringBuffer();
                    strBuff.append("grant_type=").append("client_credential");
                    strBuff.append("&appid=").append(appId);
                    strBuff.append("&secret=").append(secret);
                    String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
                    log.info("wxOverAllAccessToken result:"+result);
                    AccessTokenThirdVO wxAccessTokenVO = JSON.parseObject(result, AccessTokenThirdVO.class);
                    RedisUtil.set(appId, wxAccessTokenVO.getAccessToken(), 5400);
                    log.info("请求微信新的accessToken：{}", wxAccessTokenVO.getAccessToken());
                    return wxAccessTokenVO.getAccessToken();
                } catch (Exception e) {
                    throw e;
                } finally {
                    redissonDistributedLocker.unlock(LOCK_ACCESS_TOKEN);
                }
            }
        }
        return String.valueOf(accessToken);
    }


    /**
     * 获取微信用户信息
     *
     * @param accessToken
     * @param openId
     * @return
     */
    @Override
    public UserInfoThirdVO getUserInfo(AppIdKeyDTO appIdKeyDTO, String accessToken, String openId) {

        String requestUrl = authConfig.getWxUserinfoUrl();
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("access_token=").append(accessToken);
        strBuff.append("&openid=").append(openId);
        log.info("获取微信用户个人信息param:{}", strBuff.toString());
        String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
        log.info("userinfo result:{}", result);
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject != null) {
            UserInfoThirdVO wxUserInfoVO = new UserInfoThirdVO();
            wxUserInfoVO.setOpenId(jsonObject.getString("openid"));
            wxUserInfoVO.setNickName(jsonObject.getString("nickname"));
            wxUserInfoVO.setSex(jsonObject.getString("sex"));
            wxUserInfoVO.setHeadimgUrl(jsonObject.getString("headimgurl"));
            wxUserInfoVO.setUnionid(jsonObject.getString("unionid"));
            return wxUserInfoVO;
        } else {
            return null;
        }
    }

    /**
     * 获取ticket
     *
     * @return
     */
    @Override
    public ShareTicketThirdVO getShareTicketVO(AppIdKeyDTO appIdKeyDTO, String url) {

        String ticketKey = "zhuanbo:ticket:" + authConfig.getWxGzhAppId();
        TicketVO ticket;
        Object o = RedisUtil.get(ticketKey);
        if (o != null) {
            ticket = (TicketVO) o;
        } else {
            String accessToken = this.getOverAllAccessToken(appIdKeyDTO, false);
            if (StringUtils.isBlank(accessToken)) { // 获取 accessToken 失败
                log.error("获取微信accessToken失败:{}", accessToken);
                return null;
            }
            ticket = getTicket(authConfig.getWxTicketUrl(), accessToken);
            if (!Integer.valueOf(0).equals(ticket.getErrcode())) {
                log.error("无效的ticket，开始刷新");
                int i = 3;
                while (i > 0 && (!Integer.valueOf(0).equals(ticket.getErrcode()))) {
                    RedisUtil.del(authConfig.getWxGzhAppId());
                    accessToken = this.getOverAllAccessToken(appIdKeyDTO, true);
                    ticket = getTicket(authConfig.getWxTicketUrl(), accessToken);
                    i--;
                }
                if (!Integer.valueOf(0).equals(ticket.getErrcode())) {
                    log.error("无效的ticket,重试3次也不行");
                    return null;
                }
            }
            RedisUtil.set(ticketKey, ticket, 120);
        }

        String noncestr = CharUtil.getRandomString(16);
        Integer timestamp = DateUtil.getSecondTimestamp(System.currentTimeMillis());

        StringBuffer sha1StrBuff = new StringBuffer();
        sha1StrBuff.append("jsapi_ticket=").append(ticket.getTicket());
        sha1StrBuff.append("&noncestr=").append(noncestr);
        sha1StrBuff.append("&timestamp=").append(timestamp);
        sha1StrBuff.append("&url=").append(url);
        String signature = CheckSumBuilder.getSha1(sha1StrBuff.toString());
        log.info("sha1StrBuff: {}" , sha1StrBuff.toString());
        log.info("signature: {}" , signature);
        ShareTicketThirdVO wxShareTicketVO = new ShareTicketThirdVO();
        wxShareTicketVO.setAppId(authConfig.getWxGzhAppId());
        wxShareTicketVO.setTimestamp(timestamp);
        wxShareTicketVO.setNonceStr(noncestr);
        wxShareTicketVO.setSignature(signature);
        return wxShareTicketVO;
    }

    @Override
    public String code2openid(AppIdKeyDTO appIdKeyDTO, String code) throws Exception {
        if (ConstantsEnum.CODE_SOURCE_GZH.stringValue().equals(appIdKeyDTO.getSource())) {// 微信网页
            return code2openidForPage(authConfig.getWxOauth2AccessTokenUrl(), authConfig.getWxGzhAppId(), authConfig.getWxGzhSecret(), code);
        } else if (ConstantsEnum.CODE_SOURCE_MP.stringValue().equalsIgnoreCase(appIdKeyDTO.getSource())) {
            SessionKeyOpenIdDTO sessionKeyOpenIdDTO = mpSessionKeyDTO(code, null);
            if (StringUtils.isBlank(sessionKeyOpenIdDTO.getOpenid())) {
                throw new ShopException(10061);
            }
            return sessionKeyOpenIdDTO.getOpenid();
        }
        return null;
    }

    @Override
    public TicketVO getTicket(String url, String accessToken) {

        String requestUrl = authConfig.getWxTicketUrl();
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("access_token=").append(accessToken);
        strBuff.append("&type=").append("jsapi");
        log.info("请求微信jsapiTicket:url{}", strBuff.toString());

        String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
        log.info("请求微信jsapiTicket:result{}", result);
        if (StringUtils.isBlank(result)) {
            return new TicketVO();
        }
        return JSON.parseObject(result, TicketVO.class);
    }

    @Override
    public String mpAccessToken() {

        Object o = RedisUtil.get(MP_MPAMLL_ACCESSTOKEN);
        if (o != null) {
            return o.toString();
        }
        String requestUrl = authConfig.getWxAccessTokenUrl();
        String secret = authConfig.getWxMpSecret();
        String appId = authConfig.getWxMpAppId();

        StringBuffer strBuff = new StringBuffer();
        strBuff.append("grant_type=").append("client_credential");
        strBuff.append("&appid=").append(appId);
        strBuff.append("&secret=").append(secret);
        log.info("小程序accesstoken请求信息:{}", requestUrl + strBuff.toString());
        String result = HttpUtil.sendGet(requestUrl, strBuff.toString());
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject != null && jsonObject.containsKey("access_token")) {
            String accessToken = jsonObject.getString("access_token");
            RedisUtil.set(MP_MPAMLL_ACCESSTOKEN, accessToken, 3600);// 一小时
            return accessToken;
        }
        log.error("获取小程序accessToken失败：{}", result);
        throw new ShopException("获取小程序accessToken失败");
    }

    @Override
    public String getliveinfo(Map data) throws Exception {
        String accessToken = mpAccessToken();
        String url = authConfig.getGetliveinfo()+ "?access_token=" + accessToken;
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("content-type", "application/json;application/x-www-form-urlencoded;charset=utf-8");
        return HttpUtil.sendPostJson(url, data, headers);
    }

    @Override
    public String mpMobile(String code, String encryptedData, String iv) {
        String sessionKey = mpSessionKey(code);
        log.info("解密前的信息：{},{},{}", sessionKey, encryptedData, iv);
        String s = WxMaCryptUtils.decryptAnotherWay(sessionKey, encryptedData, iv);
        log.info("解密后的信息：{}",s );
        WxMaPhoneNumberInfo wxMaPhoneNumberInfo = WxMaPhoneNumberInfo.fromJson(s);
        /*String s = decryptMP(sessionKey, encryptedData, iv);
        log.info("解密后的信息：{}",s );*/
        return wxMaPhoneNumberInfo.getPurePhoneNumber();
    }

    @Override
    public String mpSessionKey(String code) {

        StringBuffer key = new StringBuffer(RedisUtil.KEY_PRE);
        key.append(authConfig.getWxMpAppId()).append(authConfig.getWxMpSecret()).append(code);
        Object o = redisTemplate.opsForValue().get(key.toString());
        if (o != null) {
            return o.toString();
        }
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("appid=").append(authConfig.getWxMpAppId());
        strBuff.append("&secret=").append(authConfig.getWxMpSecret());
        strBuff.append("&js_code=").append(code);
        strBuff.append("&grant_type=").append("authorization_code");
        String result = HttpUtil.sendGet(authConfig.getMpSessionUrl(), strBuff.toString());
        log.info("code|{}|获取小程序SessionKey|{}", code, result);
        String sessionKey = JSON.parseObject(result).getString("session_key");
        if (StringUtils.isNotBlank(sessionKey)) {
            redisTemplate.opsForValue().set(key.toString(), sessionKey, 5, TimeUnit.MINUTES);
            return sessionKey;
        }
        log.error("获取sessionKey失败");
        throw new ShopException(10067);
    }

    @Override
    public SessionKeyOpenIdDTO mpSessionKeyDTO(String code, String sessionKeyStr) {

        SessionKeyOpenIdDTO sessionKeyOpenIdDTO = getCacheSessionKeyOpenIdDTO(sessionKeyStr);
        if (sessionKeyOpenIdDTO != null) {
            return sessionKeyOpenIdDTO;
        }

        StringBuffer strBuff = new StringBuffer();
        strBuff.append("appid=").append(authConfig.getWxMpAppId());
        strBuff.append("&secret=").append(authConfig.getWxMpSecret());
        strBuff.append("&js_code=").append(code);
        strBuff.append("&grant_type=").append("authorization_code");
        String result = HttpUtil.sendGet(authConfig.getMpSessionUrl(), strBuff.toString());
        log.info("code|{}|获取小程序SessionKey|{}", code, result);
        String sessionKey = JSON.parseObject(result).getString("session_key");
        if (StringUtils.isNotBlank(sessionKey)) {
            sessionKeyOpenIdDTO = JSON.parseObject(result, SessionKeyOpenIdDTO.class);
            if (StringUtils.isBlank(sessionKeyStr)) {
                sessionKeyStr = UUID.randomUUID().toString();
            }
            sessionKeyOpenIdDTO.setSessionKeyStr(sessionKeyStr);
            cacheSessionKeyOpenIdDTO(sessionKeyStr, sessionKeyOpenIdDTO);
            return sessionKeyOpenIdDTO;
        }
        log.error("获取sessionKey失败");
        throw new ShopException(10067);
    }

    @Override
    public Map<String, Object> mpMobileMap(String code, String encryptedData, String iv) {

        SessionKeyOpenIdDTO sessionKeyOpenIdDTO = mpSessionKeyDTO(code, null);
        String s = WxMaCryptUtils.decryptAnotherWay(sessionKeyOpenIdDTO.getSession_key(), encryptedData, iv);
        log.info("解密后的信息：{}",s );
        WxMaPhoneNumberInfo wxMaPhoneNumberInfo = WxMaPhoneNumberInfo.fromJson(s);
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("mobile", wxMaPhoneNumberInfo.getPurePhoneNumber());
        RedisUtil.set(MapKeyEnum.ZBMALL_OPENID_BEAN.value() + wxMaPhoneNumberInfo.getPurePhoneNumber(), sessionKeyOpenIdDTO, 300);
        return stringObjectHashMap;
    }

    @Override
    public WxMaPhoneNumberInfo mpMobileInfo(MobileMPMobileDTO mobileMPMobileDTO) {

        SessionKeyOpenIdDTO sessionKeyOpenIdDTO = getCacheSessionKeyOpenIdDTO(mobileMPMobileDTO.getSessionKeyStr());
        sessionKeyOpenIdDTO = sessionKeyOpenIdDTO == null ? mpSessionKeyDTO(mobileMPMobileDTO.getCode(), mobileMPMobileDTO.getSessionKeyStr()) : sessionKeyOpenIdDTO;

        String s;
        try {
            s = WxMaCryptUtils.decryptAnotherWay(sessionKeyOpenIdDTO.getSession_key(), mobileMPMobileDTO.getEncryptedData(), mobileMPMobileDTO.getIv());
        } catch (Exception e) {
            log.error("小程序手机解密失败:{}", e);
            throw new ShopException(10067);
        }
        cacheSessionKeyOpenIdDTO(sessionKeyOpenIdDTO.getSessionKeyStr(), sessionKeyOpenIdDTO);

        log.info("解密后的信息：{}",s );
        WxMaPhoneNumberInfo wxMaPhoneNumberInfo = WxMaPhoneNumberInfo.fromJson(s);
        return wxMaPhoneNumberInfo;
    }

    @Override
    public WxMaUserInfo mpUserInfo(String code, String encryptedData, String iv) {
        SessionKeyOpenIdDTO sessionKeyOpenIdDTO = mpSessionKeyDTO(code, null);
        String s = WxMaCryptUtils.decryptAnotherWay(sessionKeyOpenIdDTO.getSession_key(), encryptedData, iv);
        WxMaUserInfo wxMaUserInfo = WxMaUserInfo.fromJson(s);
        log.info("小程序用户信息|{}", s);
        return wxMaUserInfo;
    }

    @Override
    public WxMaUserInfo mpUserInfoBySessionKey(String sessionKey, String encryptedData, String iv) {
        String s;
        WxMaUserInfo wxMaUserInfo;
        try {
            s = WxMaCryptUtils.decryptAnotherWay(sessionKey, encryptedData, iv);
            wxMaUserInfo = WxMaUserInfo.fromJson(s);
        } catch (Exception e) {
            log.error("小程序用户信息解密失败:{}", e);
            throw new ShopException(10067);
        }
        log.info("小程序用户信息|{}", s);
        return wxMaUserInfo;
    }

    @Override
    public void cacheSessionKeyOpenIdDTO(String sessionKeyStr, SessionKeyOpenIdDTO sessionKeyOpenIdDTO) {
        redisTemplate.opsForValue().set(sessionKeyStr, sessionKeyOpenIdDTO, 10, TimeUnit.MINUTES);
    }

    @Override
    public SessionKeyOpenIdDTO getCacheSessionKeyOpenIdDTO(String sessionKeyStr) {
        if (StringUtils.isBlank(sessionKeyStr)) {
            return null;
        }
        Object o = redisTemplate.opsForValue().get(sessionKeyStr);
        if (o == null) {
            return null;
        }
        return (SessionKeyOpenIdDTO) o;
    }

    private String code2openidForPage(String url, String appId, String appSecret, String code) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("appid=").append(appId);
        stringBuffer.append("&secret=").append(appSecret);
        stringBuffer.append("&code=").append(code);
        stringBuffer.append("&grant_type=").append("authorization_code");
        log.info("code2openidForPage:url:{}", stringBuffer.toString());
        String result = HttpUtil.sendGet(url, stringBuffer.toString());
        log.info("code2openidForPage:result:{}", result);
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject.get("errcode") != null) {
            throw new ShopException(10071);
        }
        return jsonObject.getString("openid");
    }

    private String decryptMP(String sessionKey, String encryptedData, String iv) {
        byte[] keyBytes = Base64.decodeBase64(sessionKey.getBytes(UTF_8));
        int base = 16;
        if (keyBytes.length % base != 0) {
            int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
            byte[] temp = new byte[groups * base];
            Arrays.fill(temp, (byte)0);
            System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
            keyBytes = temp;
        }
        Security.addProvider(new BouncyCastleProvider());
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES-128-CBC");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            cipher.init(2, key, new IvParameterSpec(Base64.decodeBase64(iv.getBytes(UTF_8))));
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedData.getBytes(UTF_8))), UTF_8);
        } catch (Exception var7) {
            throw new RuntimeException("AES解密失败！", var7);
        }
    }
}
