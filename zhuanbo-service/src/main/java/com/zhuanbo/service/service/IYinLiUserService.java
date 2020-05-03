package com.zhuanbo.service.service;

import com.zhuanbo.core.dto.AdminUserDTO;

/**
 * @author Administrator
 * @title: IYinLiUserService
 * @description: TODO
 * @date 2020/4/24 13:02
 */
public interface IYinLiUserService {

    /**
     * 根据手机号, 区号获取用户信息
     * @param adminUserDTO
     * @return
     */
    AdminUserDTO getUserByMobileAndAreaCode(AdminUserDTO adminUserDTO);
}
