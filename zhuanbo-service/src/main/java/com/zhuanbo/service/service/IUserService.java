package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.constants.MQDataTypeEnum;
import com.zhuanbo.core.dto.AdminCheckUserDTO;
import com.zhuanbo.core.dto.AdminModifyUser;
import com.zhuanbo.core.dto.AdminRealNameDTO;
import com.zhuanbo.core.dto.AdminUserDTO;
import com.zhuanbo.core.dto.MobileStaUserTeamDTO;
import com.zhuanbo.core.dto.MqUserLevelDTO;
import com.zhuanbo.core.dto.RegisterDTO;
import com.zhuanbo.core.dto.WxUserDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.vo.UserUpgradePointVO;
import com.zhuanbo.service.vo.CodeParamsVO;
import com.zhuanbo.service.vo.LoginRegisterResultVO;
import com.zhuanbo.service.vo.RealNameVO;
import com.zhuanbo.service.vo.UserLoginVO;
import com.zhuanbo.service.vo.UserVO;
import com.zhuanbo.service.vo.WxTokenVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IUserService extends IService<User> {
    /**
     * 发送验证码
     * @param template
     * @param areaCode 国家区域编号
     * @param mobile
     * @return
     */
    String sendMobileCode(String template, String areaCode, String mobile, String platForm);

    /**
     * 封装登录的用户信息
     * @param user
     * @param makeUserToken true:创建新的userToken
     * @return
     */
    UserLoginVO packageUser(User user, boolean makeUserToken);

    /**
     * 手动分页
     * @param page
     * @param ew
     * @return
     */
    IPage pageManual(IPage<UserVO> page, Map<String, Object> ew);

    /**
     * 根据userId获取所有上级，并对上级进行判断升级
     * @param userId
     */
    void upgradeLevel(Long userId);
    
    /**
     * 获取用户userId和Level等级列表
     * @param params
     * @return
     */
    List<Object> listUserIdAndLevel(Map<String, Object> params);
    
    /**
     *  获取用户userId和Level等级
     * @param userId
     * @return
     */
    Object getUserIdAndLevel(Long userId);

    /**
     * 删除用户信息
     * @param id 用户id
     */
    void removeUserCache(Serializable id);

    /**
     * 清除缓存
     * @param ids
     */
    void removeUserCacheList(Collection<Long> ids);
    /**
     * 生成邀请码
     * 主要流程：
     *  1、checkInviteCodeNumber()定时生成一批邀请码放入redis.list，同时过滤掉现在有的
     *  2、generateInviteCode()从中获取
     * @return
     */
    String generateInviteCode();

    /**
     * 是否要添加新的邀请码到缓存里
     */
    void checkInviteCodeNumber();

    /**
     * 根据邀请码获取用户（邀请码可能是手机）
     * @param inviteCode
     * @return
     */
    User findByInviteCode(String inviteCode);

    /**
     * 实名认证
     * @param map 请求参数
     * @return
     */
    RealNameVO realName(Map<String, Object> map);

    /**
     * 实名认证信息列表
     * @return
     */
   Object realNameList(AdminRealNameDTO dto)throws Exception;
    
    /**
     * @Description(描述):    团队(包含团队)
     * @auther: Jack Lin
     * @param :[statUserTeamDTO]
     * @return :java.lang.Object
     * @date: 2019/8/17 11:54
     */
    Map<String, Object> team(MobileStaUserTeamDTO statUserTeamDTO)throws Exception;
    /**
     * @Description(描述):    合团队人数统计(包含团队)
     * @auther: Jack Lin
     * @param :[statUserTeamDTO]
     * @return :java.lang.Object
     * @date: 2019/8/17 11:54
     */
    Map<String, Object> teamCount(MobileStaUserTeamDTO statUserTeamDTO)throws Exception;

    /**
     * @Description(描述):    更新用户信息，屏蔽/解除屏蔽/封禁/解除封禁/用户登记变更
     * @auther: Jack Lin
     * @param :[dto]
     * @return :java.lang.Object
     * @date: 2019/8/30 15:21
     */
    Map<String, Object> updateUser(AdminUserDTO dto)throws Exception;

    /**
     * M星人快速升级调整pt_no,pt_level,invite_code
     * @param user 用户
     * @param level 要调整的级别
     */
    void start2High2Gift(User user, Integer level);

    /**
     * 用户提现绑定银行卡列表
     * @param dto AdminUserDTO
     * @return
     */
    Object withdrBankList(AdminUserDTO dto) throws Exception ;

    /**
     * 用户快捷绑定银行卡列表
     * @param dto AdminUserDTO
     * @return
     */
    Object quickBankList(AdminUserDTO dto) throws Exception ;

    /**
     * 用户实名-置为无效
     * @param userId 用户id
     * @param adminId 管理平台登录用户id
     * @return
     * @throws Exception
     */
    Object cancelRealname(Integer adminId, Long userId) throws Exception;

    /**
     * 公共注册方法
     * @param registerDTO
     */
    LoginRegisterResultVO commonRegister(RegisterDTO registerDTO);

    /**
     * 注册与登录时用到
     * @param registerDTO
     * @param pid 邀请上级
     */
    User mpCommonRegister(RegisterDTO registerDTO, Long pid);

    /**
     * 校验手机号和验证码是否OK
     * @param mobile 手机
     * @param code 验证码
     * @return
     */
    void checkMobileCode(String mobile, String code, RegisterDTO registerDTO);

    /**
     * 获取邀请上级
     * @param codeParamsVO
     * @return
     */
    Long getInvitePid(CodeParamsVO codeParamsVO);

    /**
     * 判断手机是否存在(如果手机号存在，抛异常)
     * @param mobile 手机号
     */
    boolean isNotRegisterMobileThrowEx(String mobile);

    /**
     * test
     * @param id
     * @return
     */
    Object testcc(Long id);

    /**
     * 根据mq消息处理用户信息
     * @param mqUserLevelDTO
     */
    Map<String, Object> updateUserByMQ(MqUserLevelDTO mqUserLevelDTO);

    @Transactional
    void modifyMobile(Integer adminId, AdminModifyUser adminModifyUser);

    /**
     * 账户信息
     * @param id
     * @return
     */
    UserVO accounInfo(Integer id);

    /**
     * 同步用户到live
     * @param user
     */
    void synchronize2Live(User user);

    /**
     * 登录信息返回
     * @param user
     * @param makeUserToken 是否生成token
     * @return
     */
    UserLoginVO userLoginData(User user, boolean makeUserToken);

    /**
     * 生成mq消息,用于同步
     * @param mqDataTypeEnum
     * @param user
     * @param insertDB 是否入库
     * @return[{action:action, data:{}] action:对应IRabbitMQSenderService.action, data对应IRabbitMQSenderService.data
     */
    List<Map<String, Object>> makeMqDataList(User user, boolean insertDB, MQDataTypeEnum... mqDataTypeEnum);

    /**
     * 所有的注册入口都经过这里
     * @param registerDTO
     * @return
     */
    LoginRegisterResultVO registerEntrance(RegisterDTO registerDTO,Boolean backUserLoginData);

    /**
     * 处理注册后的事
     * @param loginRegisterResultVO
     */
    void afterRegister(LoginRegisterResultVO loginRegisterResultVO);

    /**
     * loginmp入口
     * @param registerDTO
     * @return
     */
    LoginRegisterResultVO loginMPEntrance(RegisterDTO registerDTO);

    /**
     * 手机号和邀请码处理（可能要扩展）
     * @param mobile
     * @param inviteCode
     * @param withoutCode
     */
    void registerCheckMobileNCode(String mobile, String inviteCode, Integer withoutCode);


    LoginRegisterResultVO registerNewWXUser(String appVersion, WxTokenVO wxTokenVO, RegisterDTO registerDTO, String sysCnl);

    LoginRegisterResultVO bindByWX(String appVersion, RegisterDTO registerDTO, HttpServletRequest request);

    List<User> xxx();

    /**
     * 小程序绑定unionid
     * @param registerDTO
     */
    UserLoginVO bindMpUnionid(RegisterDTO registerDTO);

    /**
     * 是否要绑定unionid
     * @param sysCnl 处理目标H5、WEB、WX-APPLET、WX-PUBLIC
     * @param uid 用户id
     * @param mobile 用户mobile
     * @return null 不需要
     */
    Map<String, Object> needUnionid(String sysCnl, Long uid, String mobile);

    IPage wxUserList(IPage<WxUserDTO> page, WxUserDTO params);

    /**
     * 通过管理平台创建用户
     */
    AdminCheckUserDTO managementCheckUser(Long adminId, AdminUserDTO adminUserDTO, HttpServletRequest request);

    /**
     * 通过管理平台创建用户
     */
    Map<String, Object> managementCreateUser(Long adminId, AdminUserDTO adminUserDTO, HttpServletRequest request);

    /**
     * 根据手机号或者授权号获取用户信息
     *
     * @param mobile
     * @param areaCode
     * @param authNo
     * @return
     */
    User getByIdOrMobileOrAuthNo(Long userId, String mobile, String areaCode, String authNo);

    /**
     * 修改用户等级
     *
     * @param adminId
     * @param adminUserDTO
     * @return
     */
    Map<String, Object> managementEditPtlevel(Long adminId, AdminUserDTO adminUserDTO);

    /**
     * 修改邀请上级
     *
     * @param adminId
     * @param adminUserDTO
     * @return
     */
    UserUpgradePointVO managementUpgradePoint(Long adminId, AdminUserDTO adminUserDTO);

}
