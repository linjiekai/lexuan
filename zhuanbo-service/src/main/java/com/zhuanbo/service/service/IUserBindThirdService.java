package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.UserBindThird;

/**
 * <p>
 * 用户绑定第三方账号表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
public interface IUserBindThirdService extends IService<UserBindThird> {

    /**
     * 根据uid,bindType获取信息
     * @param uid
     * @param bindType
     * @return
     */
    UserBindThird findOne(Long uid, String bindType);

    /**
     * 根据unionid或bindType获取数据
     * @param unionid
     * @param bindType
     * @return
     */
    UserBindThird findOneByUnionidOrBindType(String unionid, String bindType);

    /**
     * 复制一份
     * @param userBindThird
     * @param bindType
     */
    void copyOne(UserBindThird userBindThird, String bindType, String openid);
}
