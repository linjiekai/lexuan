package com.zhuanbo.external.service.wx.service;


import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.wx.vo.AccessTokenThirdVO;
import com.zhuanbo.external.service.wx.vo.ShareTicketThirdVO;
import com.zhuanbo.external.service.wx.vo.TicketVO;
import com.zhuanbo.external.service.wx.vo.UserInfoThirdVO;

public interface IThirdService {

    UserInfoThirdVO mpLogin(AppIdKeyDTO appIdKeyDTO, String code, String encryptedData, String iv) throws Exception;

    AccessTokenThirdVO getAccessTokenVO(AppIdKeyDTO appIdKeyDTO, String code, String platform) throws Exception;

    /**
     * 获取token
     * @param appIdKeyDTO
     * @return
     */
    String getOverAllAccessToken(AppIdKeyDTO appIdKeyDTO, boolean delCache);

    UserInfoThirdVO getUserInfo(AppIdKeyDTO appIdKeyDTO, String accessToken, String openId) throws Exception;


    ShareTicketThirdVO getShareTicketVO(AppIdKeyDTO appIdKeyDTO, String url);

    String code2openid(AppIdKeyDTO appIdKeyDTO, String code) throws Exception;

    TicketVO getTicket(String url, String accessToken);
    
}
