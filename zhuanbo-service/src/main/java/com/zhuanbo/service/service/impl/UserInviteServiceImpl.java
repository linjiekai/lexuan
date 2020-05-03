package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.UserEnum;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.mapper.UserInviteMapper;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用户邀请关系表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
@Slf4j
public class UserInviteServiceImpl extends ServiceImpl<UserInviteMapper, UserInvite> implements IUserInviteService {

    private final Long ZERO = 0L;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IDictionaryService iDictionaryService;
    /*@Autowired
    private IGraphService iGraphService;*/

    @Override
    public List<Object> getChildren(Long userId) throws Exception {
        User user = iUserService.getById(userId);
        // List<GUser> gUserList = iGraphService.directChildren(user.getId().toString(), user.getPtLevel());
        /*if (CollectionUtils.isEmpty(gUserList)) {
            return Lists.newArrayList();
        }*/
        //List<Object> idLevels = gUserList.stream().map(x -> x.getId() + "," + x.getPtLevel()).collect(Collectors.toList());
        //return idLevels;
        return new ArrayList<>();
        // 旧代码
        /*//M星人直接结束
        if (null == user || user.getPtLevel() == PtLevelType.PLAIN.getId()) {
            return new ArrayList<>();
        }

        //Dictionary dictionary = iDictionaryService.getOne(new QueryWrapper<Dictionary>().eq("category", "mallUser").eq("name", "companyUserId"));
        //如果等于公司商城账号，直接返回空
        if (user.getId().longValue() == iDictionaryService.findCompanyIdCache()) {
            return new ArrayList<>();
        }
        //查直属下级 用户
        List<UserInvite> list = iUserInviteService.list(new QueryWrapper<UserInvite>().select("id").eq("pid", userId));
        if (null == list || list.size() <= 0) {
            return new ArrayList<>();
        }
        List<Long> userIds = list.stream().map(ui -> ui.getId()).collect(Collectors.toList());
        Map<String, Object> params = new HashMap<>();
        params.put("userIds", userIds);

        //查出所有 子用户 层级
        List<Object> childrenList = iUserService.listUserIdAndLevel(params);
        if (null == childrenList) {
            return new ArrayList<>();
        }

        return childrenList;*/
    }

    @Override
    public void getChildrenAll(List<Object> data, Long userId) throws Exception {

        /*List<GUser> gUserList = iGraphService.allChildrens(userId.toString());
        if (CollectionUtils.isNotEmpty(gUserList)) {
            List<Object> collect = gUserList.stream().map(x -> x.getId() + "," + x.getPtLevel()).collect(Collectors.toList());
            data.addAll(collect);
        }*/
        /*
        User u = iUserService.getById(userId);
        List<GUser> gUserList = iGraphService.childrenLessByPtlevel(userId.toString(), u.getPtLevel(), ptLevel);
        if (CollectionUtils.isNotEmpty(gUserList)) {
            List<Object> collect = gUserList.stream().map(x -> x.getId() + "," + x.getPtLevel()).collect(Collectors.toList());
            data.addAll(collect);
        }*/
        // 旧代码
        /*List<Object> childrenList = this.getChildren(userId);

        if (null == childrenList || childrenList.size() <= 0) {
            return;
        }

        String[] children = null;
        Integer childrenPtLevel = null;
        for (Object obj : childrenList) {
            children = obj.toString().split(",");
            userId = Long.parseLong(children[0]);
            childrenPtLevel = Integer.parseInt(children[1]);
            //同级别的下级也一起查出来
            if (ptLevel >= childrenPtLevel) {
                data.add(obj);
                if (childrenPtLevel == PtLevelType.PLAIN.getId()) {
                    continue;
                }
                getChildrenAll(data, userId, ptLevel);
            }

        }*/
    }

    @Override
    public void getTeam(List<Object> data, Long userId) throws Exception {
    	
    	/*User u = iUserService.getById(userId);
        List<GUser> gUserList = iGraphService.allTeamList(u);
        
    	data = gUserList.stream().map(x -> x.getId() + "," + x.getPtLevel()).collect(Collectors.toList());*/
    }

    @Override
    public List<Long> getTeamFilterId(Long userId) throws Exception {
        return new ArrayList<>();
        /*User u = iUserService.getById(userId);
        List<GUser> gUserList = iGraphService.allTeamList(u);
        List<Long> ids = gUserList.stream().filter(x -> StringUtils.isNotBlank(x.getId())).map(x -> Long.valueOf(x.getId())).collect(Collectors.toList());
        return ids;*/
        // 旧写法
        /*List<Object> data = new ArrayList<Object>();
        getTeam(data, userId);

        List<Long> collect = data.stream().map(x -> {
            if (x == null) {
                return null;
            } else {
                return Long.parseLong(String.valueOf(x).split(",")[0]);
            }
        }).filter(x -> x != null).collect(Collectors.toList());

        return collect;*/
    }

    @Override
    public int immediateNumber(Long userId, Integer ptLevel) {
        return baseMapper.immediateNumber(userId, ptLevel);
    }

    @Override
    public int directLevelUserNumber(Long userId, Integer level) {
        return baseMapper.directLevelUserNumber(userId, level);
    }

    @Override
    public Long findInviteUserIdByOrder(Order order) {

        if (ZERO.equals(order.getInviteUserId()) || order.getInviteUserId() == null) {
            UserInvite userInvite = getById(order.getUserId());
            return userInvite.getPid();
        }
        return order.getInviteUserId();
    }

    @Override
    public Long getProfitPid(Order order, User user, UserInvite userInvite) {
        LogUtil.SHARE_PROFIT.info("XXXXXXXXXXX:{},{}, {}", order, user, userInvite);
        Long companyUid = iDictionaryService.findForLong(ConstantsEnum.MALL_USER.stringValue(), ConstantsEnum.COMPANY_USER_ID.stringValue());
        if (ZERO.equals(order.getInviteUserId()) || companyUid.equals(order.getInviteUserId())) {
            if (userInvite == null) {
                return companyUid;
            }
            return userInvite.getPid();
        } else {
            if (userInvite == null) {
                return companyUid;
            }
            if (userInvite.getPid().equals(order.getInviteUserId())) {
                return userInvite.getPid();
            } else {
                if (UserEnum.PT_LEVEL_0.Integer().equals(user.getPtLevel())) {
                    return order.getInviteUserId();
                } else {
                    return userInvite.getPid();
                }
            }
        }
    }

    @Override
    public Long findInvitePidN(Order order, User user, UserInvite userInvite) {
        Long invitePid;
        Long companyUid = iDictionaryService.findForLong(ConstantsEnum.MALL_USER.stringValue(), ConstantsEnum.COMPANY_USER_ID.stringValue());
        if (ZERO.equals(order.getInviteUserId()) || companyUid.equals(order.getInviteUserId())) {
            invitePid = userInvite.getPid();
        } else {
            if (userInvite.getPid().equals(order.getInviteUserId())) {
                invitePid = userInvite.getPid();
            } else {
                if (UserEnum.PT_LEVEL_0.Integer().equals(user.getPtLevel())) {
                    invitePid = order.getInviteUserId();
                } else {
                    invitePid = userInvite.getPid();
                }
            }
        }
        return invitePid;
    }

    @Override
    public Long findFirstPidToPartner(Order order, User user, UserInvite userInvite, Long ignoreUid) throws Exception {

        Long fid;
        if (user.getPtLevel() > UserEnum.PT_LEVEL_0.Integer()) {
            fid = user.getId();
        } else {
            fid = findInvitePidN(order, user, userInvite);
        }

//        Map<String, Object> map = new ObjectMapper().convertValue(iUserLevelService.getUserLevel(fid), Map.class);
//        for (String s : UserLevelServiceImpl.USERLEVEL_PARTNER) {
//            if (map.containsKey(s)) {
//                Object o = map.get(s);
//                if (o == null || StringUtils.isBlank(o.toString()) || "0".equals(o.toString())) {
//                    continue;
//                }
//                if (ignoreUid != null && ignoreUid.toString().equals(o.toString())) {
//                    continue;
//                }
//                LogUtil.SHARE_PROFIT.info("No:{}分润上级{}", order.getOrderNo(), o);
//                return (Long) o;
//            }
//        }
        return null;
    }

    @Override
    public void doUserInvite(User user, Long pid) {

        LocalDateTime now = LocalDateTime.now();

        UserInvite userInvite = new UserInvite();
        userInvite.setId(user.getId());
        userInvite.setPid(pid);
        userInvite.setInviteMonth(DateUtil.toyyyy_MM(now));
        userInvite.setAddTime(now);
        userInvite.setUpdateTime(now);
        save(userInvite);
    }

    @Override
    public List<UserInvite> xxx() {
        return baseMapper.xxx();
    }

    @Override
    public List<Object> getParents(Long userId, boolean containSelf) {
        return new ArrayList<>();
        /*List<GUser> gUserList;
        if (containSelf) {
            gUserList = iGraphService.allParentsContainSelf(userId.toString());
        } else {
            gUserList = iGraphService.allParents(userId.toString());
        }
        if (CollectionUtils.isEmpty(gUserList)) {
            return Lists.newArrayList();
        }
        List<Object> ids = gUserList.stream().map(x -> x.getId()).collect(Collectors.toList());
        return ids;*/
        /*List<Object> list = new ArrayList<>();

        if (containSelf) {
            list.add(userId);
        }

        UserInvite userInvite = null;

        while (userId > 0) {
            userInvite = iUserInviteService.getOne(new QueryWrapper<UserInvite>().eq("id", userId));

            if (null == userInvite) {
                break;
            }

            userId = userInvite.getPid();
            list.add(userId);
        }

        return list;*/
    }

    /**
     * 某个节点所有上级
     *
     * @param userId 某个节点
     * @return [id,ptLevel, id2,ptLevel, id3,ptLevel]
     */
    public List<Object> getParentsLevelAll(Long userId, boolean containSelf) throws Exception {
        return new ArrayList<>();
        /*List<GUser> gUserList;
        if (containSelf) {
            gUserList = iGraphService.allParentsContainSelf(userId.toString());
        } else {
            gUserList = iGraphService.allParents(userId.toString());
        }
        if (CollectionUtils.isEmpty(gUserList)) {
            return Lists.newArrayList();
        }
        List<Object> idPtLevels = gUserList.stream().map(x -> x.getId() + "," + x.getPtLevel()).collect(Collectors.toList());
        return idPtLevels;*/
        // 旧的写法
        /*List<Object> list = new ArrayList<>();

        UserInvite userInvite = null;
        Object parentLevel = null;
        //是否包含自己
        if (containSelf) {
            userInvite = iUserInviteService.getOne(new QueryWrapper<UserInvite>().eq("id", userId));
            parentLevel = iUserService.getUserIdAndLevel(userId);
            list.add(parentLevel);
            userId = userInvite.getPid();
        }

        while (userId > 0) {
            userInvite = iUserInviteService.getOne(new QueryWrapper<UserInvite>().eq("id", userId));
            if (null == userInvite || userInvite.getPid() <= 0) {
                break;
            }

            parentLevel = iUserService.getUserIdAndLevel(userInvite.getPid());
            userId = userInvite.getPid();
            //元素形式：["id,peLvel"]
            list.add(parentLevel);
        }

        return list;*/
    }

}
