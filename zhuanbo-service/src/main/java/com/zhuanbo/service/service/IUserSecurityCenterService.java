package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserSecurityCenter;

/**
 * <p>
 * 用户安全中心表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
public interface IUserSecurityCenterService extends IService<UserSecurityCenter> {
    /**
     * 添加一条记录
     * @param user
     */
    void doUserSecurityCenter(User user);
}
