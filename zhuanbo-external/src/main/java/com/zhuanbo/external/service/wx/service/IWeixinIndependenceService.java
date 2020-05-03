package com.zhuanbo.external.service.wx.service;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.zhuanbo.core.dto.MobileMPMobileDTO;
import com.zhuanbo.core.dto.SessionKeyOpenIdDTO;

import java.util.Map;

/**
 * 微信独立的接口
 */
public interface IWeixinIndependenceService {
    /**
     * 获取小程序accessToken
     * @return
     */
    String mpAccessToken();

    /**
     * 获取小程序直接间
     * @param data
     * @return
     * @throws Exception
     */
    String getliveinfo(Map data) throws Exception;

    /**
     * 解密获取微信手机号
     * @param encryptedData
     * @param iv
     * @return
     */
    String mpMobile(String code, String encryptedData, String iv);

    /**
     * 小程序sessionkey获取
     * @param code 小程序的code
     * @return
     */
    String mpSessionKey(String code);

    /**
     * 小程序sessionkey获取
     * @param code 小程序的code
     * @return sessionKeyOpenIdDTO
     */
    SessionKeyOpenIdDTO mpSessionKeyDTO(String code, String sessionKeyStr);

    /**
     * 解密获取微信手机号返回map
     * @param code
     * @param encryptedData
     * @param iv
     * @return
     */
    Map<String, Object> mpMobileMap(String code, String encryptedData, String iv);

    /**
     * 解密获取微信手机号返回info
     */
    WxMaPhoneNumberInfo mpMobileInfo(MobileMPMobileDTO mobileMPMobileDTO);

    /**
     * 小程序用户信息
     * @param code
     * @param encryptedData
     * @param iv
     * @return
     */
    WxMaUserInfo mpUserInfo(String code, String encryptedData, String iv);

    /**
     * 小程序用户信息
     * @param sessionKey
     * @param encryptedData
     * @param iv
     * @return
     */
    WxMaUserInfo mpUserInfoBySessionKey(String sessionKey, String encryptedData, String iv);

    /**
     * 缓存SessionKeyOpenIdDTO
     * @param sessionKeyStr
     * @param sessionKeyOpenIdDTO
     */
    void cacheSessionKeyOpenIdDTO(String sessionKeyStr, SessionKeyOpenIdDTO sessionKeyOpenIdDTO);

    /**
     * 从缓存里获取SessionKeyOpenIdDTO
     * @param sessionKeyStr
     * @return
     */
    SessionKeyOpenIdDTO getCacheSessionKeyOpenIdDTO(String sessionKeyStr);
}
