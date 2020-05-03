package com.zhuanbo.shop.api.controller;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.dto.CodeParamsDTO;
import com.zhuanbo.core.dto.MobileMPMobileDTO;
import com.zhuanbo.core.dto.RegisterDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBindThird;
import com.zhuanbo.core.entity.enumeration.ActionEnum;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.SpringContextUtil;
import com.zhuanbo.core.util.bcrypt.BCryptPasswordEncoder;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.wx.service.IThirdService;
import com.zhuanbo.external.service.wx.service.impl.IWeixinThirdServiceImpl;
import com.zhuanbo.external.service.wx.vo.AccessTokenThirdVO;
import com.zhuanbo.external.service.wx.vo.UserInfoThirdVO;
import com.zhuanbo.service.service.IAuthService;
import com.zhuanbo.service.service.IUserBindThirdService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service.impl.UserServiceImpl;
import com.zhuanbo.service.vo.LoginRegisterResultVO;
import com.zhuanbo.service.vo.UserLoginVO;
import com.zhuanbo.service.vo.WxTokenVO;
import com.zhuanbo.shop.api.dto.req.Code2Openid;
import com.zhuanbo.shop.api.dto.resp.LoginDTO;
import com.zhuanbo.shop.api.dto.resp.WxTokenDTO;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/shop/mobile/auth")
@Validated
@Slf4j
public class MobileAuthController {
    /**
     * 微信绑定来源：微信
     */
    final String BIND_SOURCE_WX = "wx";

    private final Integer MQENABLE = 1;// 0:启用MQ

    @Autowired
    private IUserService userService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IUserBindThirdService iShopUserBindThirdService;
    @Autowired
    private IAuthService iAuthService;


    /**
     * 验证码获取
     * 1.生成4位code
     * 2.调用第三方短信通道进行手机发送
     * 1）注册，验证手机号码（a.如果判断手机号码没有注册，直接发code b.如果手机号码已注册，返回已注册状态，并且发code）
     * 2）登陆，验证手机号码,需要判断手机号码是否注册（a.手机号码已经注册，发送code  b.手机号码未注册，返回未注册状态）
     * 3）微信首次登陆，验证手机号码（a.手机号码未注册，发code  b.手机号码已注册，返回已注册状态）
     * 客户端可根据不同的action获取code
     *
     * @param codeParamsDTO
     * @return
     */

    @PostMapping("/code")
    public Object code(@RequestBody CodeParamsDTO codeParamsDTO) {

        String mobile = codeParamsDTO.getMobile();
        // 每次获取验证码时间间隔60s
        long expire = RedisUtil.getExpire(mobile);
        if (expire-240 > 0) {
            return ResponseUtil.fail("10023", "请在" + (expire-240) + "秒后再获取验证码");
        }
        //isRegister = true 手机号码已注册
        boolean isRegister = !userService.list(new QueryWrapper<User>().eq("mobile", mobile)).isEmpty();
        String smsTemplate;
        switch (ActionEnum.getByTpye(codeParamsDTO.getType())) {
            case REGISTER:
                if (authConfig.getChinaCode().equals(codeParamsDTO.getAreaCode())) {
                    smsTemplate = authConfig.getSmsTemplateRegister();
                } else {
                    smsTemplate = authConfig.getSmsTemplateRegisterOverSea();
                }
                if ("OK".equalsIgnoreCase(userService.sendMobileCode(smsTemplate, codeParamsDTO.getAreaCode(), mobile,codeParamsDTO.getPlatform()))) {
                    // 已注册：该手机号码已经注册，验证后将直接登录
                    if (isRegister) {
                        return ResponseUtil.result(10028);
                    } else {
                        return ResponseUtil.result(10004);
                    }
                } else {
                    return ResponseUtil.result(10021);
                }
            case LOGIN :
            case FINDPSW:
                if (isRegister) {
                    if (ActionEnum.LOGIN.getType().equals(codeParamsDTO.getType())) {
                        if (authConfig.getChinaCode().equals(codeParamsDTO.getAreaCode())) {
                            smsTemplate = authConfig.getSmsTemplateLogin();
                        } else {
                            smsTemplate = authConfig.getSmsTemplateLoginOverSea();
                        }
                    } else {
                        if (authConfig.getChinaCode().equals(codeParamsDTO.getAreaCode())) {
                            smsTemplate = authConfig.getSmsTemplateResetPwd();
                        } else {
                            smsTemplate = authConfig.getSmsTemplateResetPwdOverSea();
                        }
                    }
                    if ("OK".equalsIgnoreCase(userService.sendMobileCode(smsTemplate, codeParamsDTO.getAreaCode(), mobile,codeParamsDTO.getPlatform()))) {
                        return ResponseUtil.result(10014);
                    } else {
                        return ResponseUtil.result(10021);
                    }
                } else {
                    return ResponseUtil.result(10029);
                }
            case WXLOGIN:
                // 由于微信可能绑定已注册的手机号，所以开放短信功能
                if (authConfig.getChinaCode().equals(codeParamsDTO.getAreaCode())) {
                    smsTemplate = authConfig.getSmsTemplateBind();
                } else {
                    smsTemplate = authConfig.getSmsTemplateBindOverSea();
                }
                if ("OK".equalsIgnoreCase(userService.sendMobileCode(smsTemplate, codeParamsDTO.getAreaCode(), mobile,codeParamsDTO.getPlatform()))) {
                    return ResponseUtil.result(10014);
                } else {
                    return ResponseUtil.result(10021);
                }
            case MPLOGIN:
                // 由于微信可能绑定已注册的手机号，所以开放短信功能
                if (authConfig.getChinaCode().equals(codeParamsDTO.getAreaCode())) {
                    smsTemplate = authConfig.getSmsTemplateBind();
                } else {
                    smsTemplate = authConfig.getSmsTemplateBindOverSea();
                }
                if ("OK".equalsIgnoreCase(userService.sendMobileCode(smsTemplate, codeParamsDTO.getAreaCode(), mobile,codeParamsDTO.getPlatform()))) {
                    return ResponseUtil.result(10014);
                } else {
                    return ResponseUtil.result(10021);
                }

        }
        log.error("参数type值不支持,type={}", codeParamsDTO.getType());
        return ResponseUtil.fail("11111", "参数type值不支持");
    }


    /**
     * 验证码校验
     * @param codeParamsDTO
     * @return
     */
    @PostMapping("/code/check")
    public Object codeCheck(@RequestBody CodeParamsDTO codeParamsDTO) {

        if (!RedisUtil.hasKey(codeParamsDTO.getMobile())) {
            return ResponseUtil.result(10017);
        }
        if ((!codeParamsDTO.getCode().equals(RedisUtil.get(codeParamsDTO.getMobile()).toString()))) {
            return ResponseUtil.result(10019);
        }
        return ResponseUtil.ok();
    }

    /**
     * 注册
     *
     * @param
     * @return
     */
    @PostMapping("/register")
    public Object register(@RequestHeader(name = "X-MPMALL-APPVer", required = false) String appVersion, @RequestBody RegisterDTO registerDTO) {


        UserLoginVO userLoginVO = null;
        LoginRegisterResultVO loginRegisterResultVO = null;
        try {
            // 公共注册
            loginRegisterResultVO = userService.registerEntrance(registerDTO, true);
            userLoginVO = loginRegisterResultVO.getUserLoginVO();
        } catch (Exception e) {
            log.error("注册异常:{}", e);
            if (e instanceof ShopException) {
                ShopException shopException = (ShopException) e;
                return ResponseUtil.result(Integer.valueOf(shopException.getCode()));
            }
            return ResponseUtil.result(10031);
        } finally {
            userService.afterRegister(loginRegisterResultVO);
        }
        if ("method".equals(registerDTO.getSource())) {// 其他方法调用这个方法，直接返回User信息
            User user = loginRegisterResultVO.getUser();
            return ResponseUtil.ok(user);
        }
        // 4、返回DTO
        return ResponseUtil.ok(userLoginVO);
    }

    /**
     * 登录
     * @param loginDTO
     * @return
     */
    @PostMapping("/login")
    public Object login(@RequestBody LoginDTO loginDTO) {

        User user = new User();
        BeanUtils.copyProperties(loginDTO, user);

        if (loginDTO.getType().equals(2)) {
            if (StringUtils.isBlank(loginDTO.getPassword())) {
                return ResponseUtil.fail("11111", "缺少参数:password");
            }
        } else {
            if (StringUtils.isBlank(loginDTO.getCode())) {
                return ResponseUtil.fail("11111", "缺少参数:code");
            }
        }
        User dbUser = userService.getOne(new QueryWrapper<User>().eq("user_name", user.getMobile()).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));
        if(dbUser!=null&&!ConstantsEnum.USER_STATUS_1.integerValue().equals(dbUser.getStatus())){
            return ResponseUtil.result(ConstantsEnum.USER_STATUS_0.integerValue().equals(dbUser.getStatus()) ? 10032 : 10033);
        }
        switch (ActionEnum.getByTpye(loginDTO.getType())) {
            case LOGIN:
                return loginPs(user, dbUser);
            case CODELOGIN:
                CodeParamsDTO codeParamsDTO = new CodeParamsDTO();
                codeParamsDTO.setWithoutCode(1);
                return loginByCode(user, loginDTO.getCode(), dbUser, codeParamsDTO);
        }
        return ResponseUtil.fail("11111", "参数<type>的值不支持");
    }

    /**
     * 密码登录
     * @param u
     * @param dbUser 已从数据库里查询出来的用户
     * @return
     */
    public Object loginPs(User u, User dbUser) {

        User user;
        if (dbUser != null) {
            user = dbUser;
        } else {
            user = userService.getOne(new QueryWrapper<User>().eq("user_name", u.getMobile()));
            if (user == null) {
                return ResponseUtil.result(10007);
            }
        }
        if(!ConstantsEnum.USER_STATUS_1.integerValue().equals(user.getStatus())){
            return ResponseUtil.result(ConstantsEnum.USER_STATUS_0.integerValue().equals(user.getStatus()) ? 10032 : 10033);
        }
        String hexPassword;
        try {
            hexPassword = DigestUtils.sha1Hex(u.getPassword().getBytes("UTF-16LE"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if (!hexPassword.equals(user.getPassword())) {
            return ResponseUtil.result(10005);
        }

//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        if (!encoder.matches(u.getPassword(), user.getPassword())) {
//            return ResponseUtil.result(10005);
//        }
        return ResponseUtil.ok(userLoginData(user, true));
    }

    /**
     * 验证码登录
     * @param u
     * @param dbUser 已从数据库查询出来的
     * @return
     */
    public Object loginByCode(User u, String code, User dbUser, CodeParamsDTO codeParamsDTO) {

        User user;
        if (dbUser != null) {
            user = dbUser;
        } else {
            user = userService.getOne(new QueryWrapper<User>().eq("user_name", u.getMobile()));
            if (user == null) {
                return ResponseUtil.result(10004);
            }
        }
        if (codeParamsDTO != null && codeParamsDTO.getWithoutCode().equals(1)) {
            if (!RedisUtil.hasKey(u.getMobile())) {
                return ResponseUtil.result(10017);
            }
            if ((!code.equals(RedisUtil.get(u.getMobile()).toString()))) {
                return ResponseUtil.result(10019);
            }
        }
        if (codeParamsDTO.getNeedUnionid().equals(1)) {
            Map<String, Object> stringObjectMap = userService.needUnionid(codeParamsDTO.getSysCnl(), user.getId(), user.getMobile());
            if (stringObjectMap != null) {
                return ResponseUtil.result(10062, stringObjectMap);
            }
        }
        return ResponseUtil.ok(userLoginData(user, true));
    }

    /**
     * 微信登录
     * 登录
     * 	小程序登录
     * 		已登录过
     * 			返回用户信息
     * 		未登录过
     * 			创建用户,返回用户信息
     * 	微信登录
     * 		已登录过
     * 			返回用户信息
     * 		未登录过
     * 			返回token信息
     * @param wxTokenDTO
     * @return
     */
    @Transactional
    @PostMapping("/login/wx")
    public Object loginWX(@RequestBody WxTokenDTO wxTokenDTO) throws Exception{

        WxTokenVO wxTokenVO = new WxTokenVO();
        wxTokenVO.setCode(wxTokenDTO.getCode());
        wxTokenVO.setTokenId(UUID.randomUUID().toString().replace("-", ""));

        
        AppIdKeyDTO appIdKeyDTO = new AppIdKeyDTO();
        BeanUtils.copyProperties(wxTokenDTO, appIdKeyDTO);
        
        String openId;
        String unionId;
        IThirdService iThirdService = (IThirdService) SpringContextUtil.getBean("weixinThirdService");
        if (UserServiceImpl.BIND_SOURCE_MP.equals(wxTokenDTO.getSource())) {// 小程序登录

            UserInfoThirdVO wxUserInfoVO = iThirdService.mpLogin(appIdKeyDTO, wxTokenDTO.getCode(),wxTokenDTO.getEncryptedData(), wxTokenDTO.getIv());
            if (wxUserInfoVO == null) {
                log.error("小程序code获取session失败");
                return ResponseUtil.result(10035);
            }
            wxTokenVO.setSource(UserServiceImpl.BIND_SOURCE_MP);
            BeanUtils.copyProperties(wxUserInfoVO, wxTokenVO);
            openId = wxTokenVO.getOpenId();
            if (openId != null) {
                RedisUtil.set(UserServiceImpl.MP_ZBMALL_OPENID, openId);// 小程序openId
            }
            unionId = wxTokenVO.getUnionid();
        } else {// 普通微信登录或H5

            AccessTokenThirdVO wxAccessTokenVO = iThirdService.getAccessTokenVO(appIdKeyDTO, wxTokenDTO.getCode(), wxTokenDTO.getPlatform());
            if (wxAccessTokenVO == null) {
                log.error("微信登录失败");
                return ResponseUtil.result(10035);
            }
            BeanUtils.copyProperties(wxAccessTokenVO, wxTokenVO);
            wxTokenVO.setSource(wxTokenDTO.getSource());
            openId = wxTokenVO.getOpenId();
            unionId = wxTokenVO.getUnionid();
        }
        if (openId == null) {
            return ResponseUtil.result(10034);
        }
        String bindType;
        if (UserServiceImpl.BIND_SOURCE_MP.equals(wxTokenDTO.getSource())) {
            bindType = ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue();
        } else {
            if (ConstantsEnum.USER_BIND_THIRD_TYPE_H5.stringValue().equalsIgnoreCase(wxTokenDTO.getSource())) {
                bindType = ConstantsEnum.USER_BIND_THIRD_TYPE_H5.value().toString();
            } else {
                bindType = ConstantsEnum.USER_BIND_THIRD_TYPE_WEIXIN.value().toString();
            }
        }
        UserBindThird userBindThird = iShopUserBindThirdService.getOne(new QueryWrapper<UserBindThird>().eq("open_id", openId)
                .eq("bind_type", bindType));
        if (userBindThird == null) {// 缓存信息
            // 艹他妈的方案1
            /*if (ConstantsEnum.USER_BIND_THIRD_TYPE_H5.stringValue().equalsIgnoreCase(wxTokenDTO.getSource())){
                throw new ShopException(10060);
            }*/
            // 全部通过unionId找对应的用户信息
            userBindThird = iShopUserBindThirdService.getOne(new QueryWrapper<UserBindThird>().eq("bind_id", unionId));
            if (userBindThird != null) {// 复制一份数据，修改openId

                UserBindThird newUserBindThird = new UserBindThird();
                BeanUtils.copyProperties(userBindThird, newUserBindThird);
                newUserBindThird.setOpenId(openId);
                newUserBindThird.setAddTime(LocalDateTime.now());
                newUserBindThird.setUpdateTime(userBindThird.getAddTime());
                newUserBindThird.setBindType(bindType);
                newUserBindThird.setPlatform(wxTokenDTO.getPlatform());
                iShopUserBindThirdService.save(newUserBindThird);
                userBindThird = newUserBindThird;
            } else {// 没有，根据平台判断返回相关的信息
                if (BIND_SOURCE_WX.equals(wxTokenDTO.getSource()) || ConstantsEnum.USER_BIND_THIRD_TYPE_H5.stringValue().equalsIgnoreCase(wxTokenDTO.getSource())) {// 获取微信用户信息(小程序的自带了用户信息，不再去获取)
                    UserInfoThirdVO wxUserInfo = iThirdService.getUserInfo(appIdKeyDTO, wxTokenVO.getAccessToken(), wxTokenVO.getOpenId());
                    BeanUtils.copyProperties(wxUserInfo, wxTokenVO);
                }
                // 无条件登录与注册
                /*if (ConstantsEnum.PLATFORM_MPWJMALL.stringValue().equalsIgnoreCase(wxTokenDTO.getPlatform())) {
                    if (iDictionaryService.findForLong(ConstantsEnum.ACTIVITY_UPGRADE.stringValue(), ConstantsEnum.ACTIVITY_UPGRADE_CHECK.stringValue()).equals(0L)) {// 活动关闭
                        return ResponseUtil.result(10045);
                    }
                }*/
                return returnBindWxData(wxTokenVO);
            }
        }
        User user = userService.getById(userBindThird.getUserId());
        if(!ConstantsEnum.USER_STATUS_1.integerValue().equals(user.getStatus())){
            return ResponseUtil.result(ConstantsEnum.USER_STATUS_0.integerValue().equals(user.getStatus()) ? 10032 : 10033);
        }
        // 玩家资格（无条件）
        /*if (ConstantsEnum.PLATFORM_MPWJMALL.stringValue().equalsIgnoreCase(wxTokenDTO.getPlatform())) {
            if (iDictionaryService.findForLong(ConstantsEnum.ACTIVITY_UPGRADE.stringValue(), ConstantsEnum.ACTIVITY_UPGRADE_CHECK.stringValue()).equals(0L)) {// 活动关闭
                if (user.getPtLevel() < 1) {// 玩家级别不够
                    return ResponseUtil.result(10045);
                }
            }
        }*/
        return ResponseUtil.ok(userLoginData(user, true));
    }

    /**
     * 返回微信登录绑定的数据
     * @param wxTokenVO
     * @return
     */
    private Object returnBindWxData(WxTokenVO wxTokenVO){
        RedisUtil.set(wxTokenVO.getTokenId(), wxTokenVO, 60 * 10);
        RedisUtil.get(wxTokenVO.getTokenId());
        Map<String, String> map = new HashMap<>();
        map.put("tokenId", wxTokenVO.getTokenId());
        map.put("wxOpenId", wxTokenVO.getOpenId());
        return ResponseUtil.fail(10036, "微信openId与tokenId信息", map);
    }


    /**
     * 微信绑定
     * @param registerDTO
     * @return
     */
    @PostMapping("/wx/bind")
    public Object wxBind(@RequestHeader(name = "X-MPMALL-APPVer", required = false) String appVersion, @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {

        userService.checkMobileCode(registerDTO.getMobile(), registerDTO.getCode(), registerDTO);
        LoginRegisterResultVO loginRegisterResultVO = null;
        try {
            loginRegisterResultVO = userService.bindByWX(appVersion, registerDTO, request);
            if (loginRegisterResultVO.getUserLoginVO() == null) {
                if (loginRegisterResultVO.getUser() == null) {
                    loginRegisterResultVO.setUser(userService.getOne(new QueryWrapper<User>().eq("mobile", registerDTO.getMobile()).eq("deleted", ConstantsEnum.DELETED_0.integerValue())));
                }
                loginRegisterResultVO.setUserLoginVO(userService.userLoginData(loginRegisterResultVO.getUser(), true));
            }
        } catch (Exception e) {
            log.error("微信绑定失败|{}|{}", registerDTO, e);
            if (e instanceof ShopException) {
                ShopException shopException = (ShopException) e;
                return ResponseUtil.fail(shopException.getCode(), shopException.getMsg());
            }
            return ResponseUtil.fail();
        } finally {
            userService.afterRegister(loginRegisterResultVO);
        }
        return ResponseUtil.ok(loginRegisterResultVO.getUserLoginVO());
    }


    /**
     * 返回用户最终登录信息
     * @param user
     * @param makeUserToken
     * @return
     */
    private UserLoginVO userLoginData(User user, boolean makeUserToken){
    	UserLoginVO userLoginVO = userService.packageUser(user, makeUserToken);
        RedisUtil.del(user.getMobile());// 删除验证码
        userService.removeUserCache(user.getId());
        return userLoginVO;
    }


    @GetMapping("/bcrypt/password")
    public Object code(@RequestParam String password) {
        BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
        return bc.encode(password);
    }

    /**
     * 小程序登录或注册
     * @param registerDTO
     * @return
     */
    @PostMapping("/login/mp")
    public Object wxLoginOrRegister(@RequestHeader(name = "LoginVersion", required = false) String appVersion,@RequestBody RegisterDTO registerDTO){

        if (StringUtils.isBlank(appVersion)) {
            int count = userService.count(new QueryWrapper<User>().eq("mobile", registerDTO.getMobile()));
            if (count > 0) {
                User user = new User();
                user.setMobile(registerDTO.getMobile());
                CodeParamsDTO codeParamsDTO = new CodeParamsDTO();
                BeanUtils.copyProperties(registerDTO, codeParamsDTO);
                return loginByCode(user, registerDTO.getCode(), null, codeParamsDTO);
            } else {

                LoginRegisterResultVO loginRegisterResultVO = null;
                try {
                    loginRegisterResultVO = userService.loginMPEntrance(registerDTO);
                } catch (Exception e) {
                    log.error("注册异常:{}", e);
                    if (e instanceof ShopException) {
                        ShopException shopException = (ShopException) e;
                        return ResponseUtil.result(Integer.valueOf(shopException.getCode()));
                    }
                    return ResponseUtil.result(10031);
                } finally {
                    userService.afterRegister(loginRegisterResultVO);
                }
                // 提示要绑定unionid
                if (registerDTO.getNeedUnionid().equals(1)) {
                    UserLoginVO userLoginVO = loginRegisterResultVO.getUserLoginVO();
                    Map<String, Object> stringObjectMap = userService.needUnionid(registerDTO.getSysCnl(), userLoginVO.getId(), userLoginVO.getMobile());
                    if (stringObjectMap != null) {
                        return ResponseUtil.result(10062, stringObjectMap);
                    }
                }
                return ResponseUtil.ok(loginRegisterResultVO.getUserLoginVO());
            }
        } else {
            LoginRegisterResultVO loginRegisterResultVO = null;
            try {
                loginRegisterResultVO = iAuthService.loginMP(registerDTO);
                if (StringUtils.isNotBlank(loginRegisterResultVO.getSessionKeyStr())) {
                    HashMap<String, Object> stringObjectHashMap = new HashMap<>();
                    stringObjectHashMap.put("sessionKeyStr", loginRegisterResultVO.getSessionKeyStr());
                    return ResponseUtil.result(10065, stringObjectHashMap);
                }
                return ResponseUtil.ok(loginRegisterResultVO.getUserLoginVO());
            } catch (Exception e) {
                log.error("小程序登录注册失败:{}", e);
                if (e instanceof ShopException) {
                    ShopException shopException = (ShopException) e;
                    return ResponseUtil.result(Integer.valueOf(shopException.getCode()));
                }
                return ResponseUtil.result(10031);
            } finally {
                userService.afterRegister(loginRegisterResultVO);
            }
        }
    }


    @PostMapping("/code2openid")
    public Object code2openid(@RequestBody AppIdKeyDTO appIdKeyDTO) throws Exception{
    	
    	IThirdService iThirdService = (IThirdService) SpringContextUtil.getBean("weixinThirdService");
        String openid = iThirdService.code2openid(appIdKeyDTO, appIdKeyDTO.getCode());
        if (StringUtils.isBlank(openid)) {
            return ResponseUtil.result(10061);
        }
        return ResponseUtil.ok(MapUtil.of("openid", openid));
    }

    /**
     * 获取小程序accesstoken
     * @return
     */
    @PostMapping("/mp/accesstoken")
    public Object mpAccessToken(){
        IWeixinThirdServiceImpl iThirdService = (IWeixinThirdServiceImpl) SpringContextUtil.getBean("weixinThirdService");
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("accesstoken", iThirdService.mpAccessToken());
        return ResponseUtil.ok(stringObjectHashMap);
    }

    /**
     * 获取直播间
     * @return
     */
    @PostMapping("/mp/getliveinfo")
    public Object mpGetliveinfo(@RequestBody HashMap hashMap) throws Exception {
        IWeixinThirdServiceImpl iThirdService = (IWeixinThirdServiceImpl) SpringContextUtil.getBean("weixinThirdService");
        String getwxacode = iThirdService.getliveinfo(hashMap);
        Map map = JSON.parseObject(getwxacode, Map.class);
        return ResponseUtil.ok(map);
    }

    /**
     * 小程序获取手机号
     * @param mobileMPMobileDTO
     * @return
     */
    @PostMapping("/mp/mobile")
    public Object mpMobile(@RequestBody MobileMPMobileDTO mobileMPMobileDTO){
        IWeixinThirdServiceImpl iThirdService = (IWeixinThirdServiceImpl) SpringContextUtil.getBean("weixinThirdService");
        WxMaPhoneNumberInfo wxMaPhoneNumberInfo = iThirdService.mpMobileInfo(mobileMPMobileDTO);

        HashMap<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("mobile", wxMaPhoneNumberInfo.getPurePhoneNumber());

        // 时间紧，写这了
        /*User user = userService.getOne(new QueryWrapper<User>().eq("mobile", wxMaPhoneNumberInfo.getPurePhoneNumber()).eq("deleted", 0));
        if (user != null) {
            UserBindThird userBindThird = iShopUserBindThirdService.findOne(user.getId(), ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue());
            if (userBindThird == null) {
                Object o = RedisUtil.get(MapKeyEnum.ZBMALL_OPENID_BEAN.value() + wxMaPhoneNumberInfo.getPurePhoneNumber());
                SessionKeyOpenIdDTO sessionKeyOpenIdDTO = (SessionKeyOpenIdDTO) o;
                LocalDateTime now = LocalDateTime.now();
                userBindThird = new UserBindThird();
                userBindThird.setUserId(user.getId());
                userBindThird.setBindId(sessionKeyOpenIdDTO.getUnionid());
                userBindThird.setOpenId(sessionKeyOpenIdDTO.getOpenid());
                userBindThird.setAddTime(now);
                userBindThird.setUpdateTime(now);
                userBindThird.setBindType(ConstantsEnum.USER_BIND_THIRD_TYPE_WX_APPLET.stringValue());
                userBindThird.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
                userBindThird.setBindStatus(1);
                userBindThird.setRegDate(DateUtil.toyyyy_MM_dd(now));
                userBindThird.setRegTime(DateUtil.toHH_mm_ss(now));
                iShopUserBindThirdService.save(userBindThird);
            }
        }*/
        return ResponseUtil.ok(stringObjectMap);
    }

    /**
     * 根据token获取用户idk
     * @param hashMap {token:xxx}
     * @return
     */
    @PostMapping("/token2Id")
    public Object token2Id(@RequestBody HashMap hashMap){
        Object token = hashMap.get("token");
        if (token == null) {
            return ResponseUtil.badArgument();
        }
        Object o = RedisUtil.get(token.toString());
        if (o == null) {
            return ResponseUtil.fail(10502, "无效token");
        }
        return ResponseUtil.ok(MapUtil.of("id", o));
    }

    /**
     * 小程序用户绑定unionid
     * @param registerDTO
     * @return
     */
    @PostMapping("/mp/unionid")
    public Object mpUnionid(@RequestBody RegisterDTO registerDTO){
        return ResponseUtil.ok(userService.bindMpUnionid(registerDTO));
    }
}
