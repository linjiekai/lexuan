package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserInvite;

import java.util.List;

/**
 * <p>
 * 用户邀请关系表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IUserInviteService extends IService<UserInvite> {

    /**
     * 获取当前用户的所有上级
     * @param userId
     * @param containSelf 返回值包含uesrId
     * @return
     */
    List<Object> getParents(Long userId, boolean containSelf);
    
    /**
     * 获取当前用户的所有上级和等级,从缓存获取
     * @param userId
     * @param containSelf
     * @return [userId,ptLevel, userId2,ptLevel, userId3,ptLevel]
     * @throws Exception
     */
    public List<Object> getParentsLevelAll(Long userId, boolean containSelf) throws Exception;

    /**
     * 获取用户下级数据(团队内的直属的)
     * @param userId
     * @return [id1,ptLevel, id2,ptLevel, id3,ptLevel]
     * @throws Exception
     */
    public List<Object> getChildren(Long userId) throws Exception;
    
    /**
     * 获取团队下级数据，过滤ID
     * @param userId
     * @throws Exception
     */
    public List<Long> getTeamFilterId(Long userId) throws Exception;
    
    
    /**
     * 获取用户所有下级数据（团队内的，小于ptLevel的）
     * @param data
     * @param userId
     * @throws Exception
     * @return [id1,ptLevel, id2,ptLevel, id3,ptLevel]
     */
    public void getChildrenAll(List<Object> data, Long userId) throws Exception;
    
    /**
     * 获取团队数据
     * @param data
     * @param userId
     * @throws Exception
     */
    public void getTeam(List<Object> data, Long userId) throws Exception;
    
    /**
     * 根据 userId 获取直属为 xxx等级 的数量
     * @param userId
     * @param ptLevel 直属的等级
     * @return
     */
    int immediateNumber(Long userId, Integer ptLevel);

    /**
     * 某个用户下的直属平级数量
     * @param userId 某个用户
     * @param level 某个用户的平级
     * @return
     */
    int directLevelUserNumber(Long userId, Integer level);

    /**
     * 根据订单判断邀请上级是谁
     * @param order 订单
     * @return 邀请上级id
     */
    Long findInviteUserIdByOrder(Order order);

    /**
     * 根据订单获取分润上级
     * @param order 订单
     * @param user 订单用户
     * @param userInvite 用户的原来邀请关系
     * @return
     */
    Long getProfitPid(Order order, User user, UserInvite userInvite);

    /**
     * 根据订单获取某个用户的邀请上级
     * @param order 某个用户的订单
     * @param user 某个用户
     * @param userInvite 某个用户的原邀请关系
     * @return
     */
    Long findInvitePidN(Order order, User user, UserInvite userInvite);

    /**
     * 根据订单获取某个用户的分润上级
     * @param order 某个用户的订单
     * @param user 某个用户
     * @param userInvite 某个用户的原邀请关系
     * @param ignoreUid 忽然的用户id
     * @return
     */
    Long findFirstPidToPartner(Order order, User user, UserInvite userInvite, Long ignoreUid) throws Exception;

    /**
     * 添加一条记录
     * @param user
     * @param pid 邀请人id
     */
    void doUserInvite(User user, Long pid);

    List<UserInvite> xxx();
}
