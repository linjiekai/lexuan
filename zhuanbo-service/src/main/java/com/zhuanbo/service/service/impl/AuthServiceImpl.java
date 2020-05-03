package com.zhuanbo.service.service.impl;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.dto.RegisterDTO;
import com.zhuanbo.core.dto.SessionKeyOpenIdDTO;
import com.zhuanbo.core.dto.WxTokenCoreDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBindThird;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.SpringContextUtil;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.wx.service.IThirdService;
import com.zhuanbo.external.service.wx.service.impl.IWeixinThirdServiceImpl;
import com.zhuanbo.external.service.wx.vo.AccessTokenThirdVO;
import com.zhuanbo.external.service.wx.vo.UserInfoThirdVO;
import com.zhuanbo.service.service.IAuthService;
import com.zhuanbo.service.service.IUserBindThirdService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.vo.LoginRegisterResultVO;
import com.zhuanbo.service.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements IAuthService {

    @Autowired
    @Qualifier("weixinThirdService")
    private IWeixinThirdServiceImpl iWeixinThirdService;
    @Autowired
    private IUserBindThirdService iUserBindThirdService;
    @Autowired
    private IUserService iUserService;

    @Transactional
    @Override
    public LoginRegisterResultVO loginMP(RegisterDTO registerDTO) {

        SessionKeyOpenIdDTO sessionKeyOpenIdDTO = null;
        WxMaUserInfo wxMaUserInfo = null;
        if (StringUtils.isNotBlank(registerDTO.getSessionKeyStr())) {
            sessionKeyOpenIdDTO = iWeixinThirdService.getCacheSessionKeyOpenIdDTO(registerDTO.getSessionKeyStr());
        } else {
            // unionid有，直接返回
            sessionKeyOpenIdDTO = iWeixinThirdService.mpSessionKeyDTO(registerDTO.getMpCode(), registerDTO.getSessionKeyStr());
            if (StringUtils.isBlank(sessionKeyOpenIdDTO.getOpenid())) {
                throw new ShopException(10061);
            }
            // 获取unionid
            if (StringUtils.isBlank(sessionKeyOpenIdDTO.getUnionid())) {
                wxMaUserInfo = iWeixinThirdService.mpUserInfoBySessionKey(sessionKeyOpenIdDTO.getSession_key(), registerDTO.getEncryptedData(), registerDTO.getIv());
                sessionKeyOpenIdDTO.setUnionid(wxMaUserInfo.getUnionId());
                sessionKeyOpenIdDTO.setNickName(wxMaUserInfo.getNickName());
                sessionKeyOpenIdDTO.setAvatarUrl(wxMaUserInfo.getAvatarUrl());
                if (StringUtils.isBlank(sessionKeyOpenIdDTO.getUnionid())) {
                    throw new ShopException(10064);
                }
                iWeixinThirdService.cacheSessionKeyOpenIdDTO(sessionKeyOpenIdDTO.getSessionKeyStr(), sessionKeyOpenIdDTO);
            }
        }

        Optional.ofNullable(sessionKeyOpenIdDTO).orElseThrow(() -> new ShopException(10067));

        log.info("sessionKeyOpenIdDTO|{}", JSON.toJSONString(sessionKeyOpenIdDTO));

        boolean isOthersUserBindThird = false;
        UserBindThird userBindThird = iUserBindThirdService.findOneByUnionidOrBindType(sessionKeyOpenIdDTO.getUnionid(), ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue());

        if (userBindThird == null) {
            userBindThird = iUserBindThirdService.findOneByUnionidOrBindType(sessionKeyOpenIdDTO.getUnionid(), null);
            isOthersUserBindThird = true;
        }

        if (userBindThird != null) {
            if (isOthersUserBindThird) {// 通过其他bindtype + unionid获取,复制一份，返回登录信息
                iUserBindThirdService.copyOne(userBindThird, ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue(), null);
            }

            LoginRegisterResultVO loginRegisterResultVO = new LoginRegisterResultVO();
            User u = iUserService.getById(userBindThird.getUserId());
            if(u!=null && !ConstantsEnum.USER_STATUS_1.integerValue().equals(u.getStatus())){
                throw new ShopException(ConstantsEnum.USER_STATUS_0.integerValue().equals(u.getStatus()) ? 10032 : 10033);
            }
            loginRegisterResultVO.setUserLoginVO(iUserService.userLoginData(u, true));
            return loginRegisterResultVO;
        }
        // 干净的unionid
        // 下面的流程只有是绑定操作
        if (Integer.valueOf(0).equals(registerDTO.getNeedBindUnionid())) {
            // 全新的unionid, 返回绑定
            LoginRegisterResultVO loginRegisterResultVO = new LoginRegisterResultVO();
            loginRegisterResultVO.setSessionKeyStr(sessionKeyOpenIdDTO.getSessionKeyStr());
            return loginRegisterResultVO;
        }
        // 绑定
        User user = iUserService.getOne(new QueryWrapper<User>().eq("mobile", registerDTO.getMobile()).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));
        // unionid没有，手机号也没有，绑定，返回
        if (user == null) {
            LoginRegisterResultVO loginRegisterResultVO = bindUnionidWithMobile(registerDTO, sessionKeyOpenIdDTO, null);
            return loginRegisterResultVO;
        } else {
            UserBindThird oldUserBindThird = iUserBindThirdService.getOne(new QueryWrapper<UserBindThird>().eq("user_id", user.getId()));
            // unionid没有，手机有（绑定过unionid），报错
            if (oldUserBindThird != null) {
                throw new ShopException(10066);
            }
            // unionid没有，手机号有，绑定返回
            if(user != null && !ConstantsEnum.USER_STATUS_1.integerValue().equals(user.getStatus())){
                throw new ShopException(ConstantsEnum.USER_STATUS_0.integerValue().equals(user.getStatus()) ? 10032 : 10033);
            }
            LoginRegisterResultVO loginRegisterResultVO = bindUnionidWithMobile(registerDTO, sessionKeyOpenIdDTO, user);
            loginRegisterResultVO.setUserLoginVO(iUserService.userLoginData(user, true));
            return loginRegisterResultVO;
        }
    }

    @Transactional
    @Override
    public LoginRegisterResultVO bindUnionidWithMobile(RegisterDTO registerDTO, SessionKeyOpenIdDTO sessionKeyOpenIdDTO, User user) {

        LoginRegisterResultVO loginRegisterResultVO = null;
        boolean updateHeader = false;
        if (user == null) {
            loginRegisterResultVO = iUserService.registerEntrance(registerDTO, true);
            user = loginRegisterResultVO.getUser();
            updateHeader = true;
        } else {
            loginRegisterResultVO = new LoginRegisterResultVO();
        }
        // 第三方绑定关系
        UserBindThird userBindThird = new UserBindThird();
        userBindThird.setUserId(user.getId());
        userBindThird.setBindId(sessionKeyOpenIdDTO.getUnionid());
        userBindThird.setOpenId(sessionKeyOpenIdDTO.getOpenid());
        userBindThird.setAddTime(LocalDateTime.now());
        userBindThird.setUpdateTime(userBindThird.getAddTime());
        userBindThird.setBindType(ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue());
        userBindThird.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        userBindThird.setBindStatus(1);
        userBindThird.setRegDate(DateUtil.toyyyy_MM_dd(userBindThird.getAddTime()));
        userBindThird.setRegTime(DateUtil.toHH_mm_ss(userBindThird.getAddTime()));
        userBindThird.setNickname(sessionKeyOpenIdDTO.getNickName());
        userBindThird.setImgUrl(sessionKeyOpenIdDTO.getAvatarUrl());
        iUserBindThirdService.save(userBindThird);

        if  (updateHeader) {
            User u = new User();
            u.setId(user.getId());
            u.setHeadImgUrl(sessionKeyOpenIdDTO.getAvatarUrl());
            u.setNickname(sessionKeyOpenIdDTO.getNickName());
            iUserService.updateById(u);
        }
        return loginRegisterResultVO;
    }

    @Override
    public UserLoginVO userWxBind(HttpServletRequest request, Long uid, WxTokenCoreDTO wxTokenCoreDTO, AppIdKeyDTO appIdKeyDTO) {

        UserBindThird userBindThird = iUserBindThirdService.getOne(new QueryWrapper<UserBindThird>().eq("user_id", uid)
                .eq("bind_type", ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.value().toString()));
        if (userBindThird != null) {
            throw new ShopException(10038);
        }

        String unionid = null;
        IThirdService iThirdService = (IThirdService) SpringContextUtil.getBean("weixinThirdService");
        AccessTokenThirdVO wxAccessTokenVO;
        try {
            wxAccessTokenVO = iThirdService.getAccessTokenVO(appIdKeyDTO, wxTokenCoreDTO.getCode(), wxTokenCoreDTO.getPlatform());
            unionid = wxAccessTokenVO.getUnionid();
        } catch (Exception e) {
            throw new ShopException("绑定失败");
        }
        if (wxAccessTokenVO == null) {
            log.error("微信登录失败1");
            throw new ShopException(10035);
        }

        userBindThird = iUserBindThirdService.getOne(new QueryWrapper<UserBindThird>().eq("open_id", wxAccessTokenVO.getOpenId())
                .eq("bind_type", ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.value().toString()));
        if (userBindThird != null) {
            throw new ShopException(10038);
        }

        UserInfoThirdVO wxUserInfo;
        try {
            wxUserInfo = iThirdService.getUserInfo(appIdKeyDTO, wxAccessTokenVO.getAccessToken(), wxAccessTokenVO.getOpenId());
            if (StringUtils.isBlank(unionid)) {
                unionid = wxUserInfo.getUnionid();
            }
        } catch (Exception e) {
            log.error("微信登录失败2");
            throw new ShopException(10035);
        }
        if (wxUserInfo == null) {
            log.error("微信登录失败3");
            throw new ShopException(10035);
        }

        // 不能绑定其他号
        List<UserBindThird> userBindThirdList = iUserBindThirdService.list(new QueryWrapper<UserBindThird>().eq("user_id", uid));
        boolean isError = false;
        for (UserBindThird bindThird : userBindThirdList) {
            if (!bindThird.getBindId().equalsIgnoreCase(unionid)) {
                isError = true;
                break;
            }
        }
        if (isError){
            throw new ShopException(10038);
        }

        LocalDateTime now = LocalDateTime.now();
        UserBindThird newUserBindThird = new UserBindThird();
        newUserBindThird.setUserId(uid);
        newUserBindThird.setBindId(wxUserInfo.getUnionid());
        newUserBindThird.setImgUrl(wxUserInfo.getHeadimgUrl());
        newUserBindThird.setNickname(wxUserInfo.getNickName());
        newUserBindThird.setBindStatus(Integer.valueOf(ConstantsEnum.USER_BIND_THIRD_STATUS_1.value().toString()));
        newUserBindThird.setOpenId(wxUserInfo.getOpenId());
        newUserBindThird.setBindType(ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.value().toString());
        newUserBindThird.setRegDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        newUserBindThird.setRegTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        newUserBindThird.setAddTime(now);
        newUserBindThird.setUpdateTime(now);
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(userAgent)) {
            if (userAgent.contains("IOS") || userAgent.contains("ios")) {
                newUserBindThird.setSysCnl("IOS");
            } else if (userAgent.contains("android") || userAgent.contains("ANDROID")) {
                newUserBindThird.setSysCnl("ANDROID");
            }
        }
        iUserBindThirdService.save(newUserBindThird);

        User user = iUserService.getById(uid);
        UserLoginVO userLoginVO = iUserService.packageUser(user, true);
        userLoginVO.setUserToken(request.getHeader(Constants.LOGIN_TOKEN_KEY));
        return userLoginVO;
    }
}
