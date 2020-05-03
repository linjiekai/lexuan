package com.zhuanbo.service.service;

import com.zhuanbo.core.dto.RegisterDTO;
import com.zhuanbo.core.dto.SessionKeyOpenIdDTO;
import com.zhuanbo.core.dto.WxTokenCoreDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.service.vo.LoginRegisterResultVO;
import com.zhuanbo.service.vo.UserLoginVO;

import javax.servlet.http.HttpServletRequest;

public interface IAuthService{
    /**
     * 小程序登录
     * @param registerDTO
     * @return
     */
    LoginRegisterResultVO loginMP(RegisterDTO registerDTO);

    /**
     * 绑定与注册
     * @param registerDTO
     * @param sessionKeyOpenIdDTO
     * @param user
     * @return
     */
    LoginRegisterResultVO bindUnionidWithMobile(RegisterDTO registerDTO, SessionKeyOpenIdDTO sessionKeyOpenIdDTO, User user);

    /**
     * 个人中心微信绑定
     * @param wxTokenCoreDTO
     * @return
     */
    UserLoginVO userWxBind(HttpServletRequest request, Long uid, WxTokenCoreDTO wxTokenCoreDTO, AppIdKeyDTO appIdKeyDTO);
}
