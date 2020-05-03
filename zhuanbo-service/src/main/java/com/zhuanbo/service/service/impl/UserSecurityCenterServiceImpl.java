package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserSecurityCenter;
import com.zhuanbo.service.mapper.UserSecurityCenterMapper;
import com.zhuanbo.service.service.IUserSecurityCenterService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户安全中心表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@Service
public class UserSecurityCenterServiceImpl extends ServiceImpl<UserSecurityCenterMapper, UserSecurityCenter> implements IUserSecurityCenterService {

    @Override
    public void doUserSecurityCenter(User user) {
        LocalDateTime now = LocalDateTime.now();

        UserSecurityCenter userSecurityCenterTmp = getById(user.getId());
        if(null != userSecurityCenterTmp){
            userSecurityCenterTmp.setBindMobile(1);
            userSecurityCenterTmp.setAddTime(now);
            userSecurityCenterTmp.setUpdateTime(now);
            updateById(userSecurityCenterTmp);
            return;
        }

        UserSecurityCenter userSecurityCenter = new UserSecurityCenter();
        userSecurityCenter.setId(user.getId());
        userSecurityCenter.setBindMobile(1);
        userSecurityCenter.setAddTime(now);
        userSecurityCenter.setUpdateTime(now);
        save(userSecurityCenter);
    }
}
