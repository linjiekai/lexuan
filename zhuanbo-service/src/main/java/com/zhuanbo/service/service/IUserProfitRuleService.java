package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.UserProfitRule;

/**
 * <p>
 * 用户利润分配规则表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IUserProfitRuleService extends IService<UserProfitRule> {

    /**
     * 保存
     * @param adminId 操作人
     * @param string 内容
     * @param type 类型 0：基础课时399 1：名品课时600 2：合伙人9980
     */
    void create(Integer adminId, String string, Integer type);
}
