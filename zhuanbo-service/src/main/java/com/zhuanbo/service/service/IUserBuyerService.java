package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.UserBuyer;

import java.util.Map;

/**
 * <p>
 * 用户订购人信息表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-09-02
 */
public interface IUserBuyerService extends IService<UserBuyer> {

    /**
     * 校验是否有效的userBuyerId
     *
     * @param userBuyerId userBuyerId
     * @param addressId   地址
     * @return true:有效，false:无效
     */
    boolean checkAddressName(Long userBuyerId, Integer addressId);

    /**
     * 订购人列表信息
     *
     * @param page   分页信息
     * @param params 查询参数
     * @return
     */
    IPage<UserBuyer> pageCustom(Page<UserBuyer> page, Map<String, Object> params);
}
