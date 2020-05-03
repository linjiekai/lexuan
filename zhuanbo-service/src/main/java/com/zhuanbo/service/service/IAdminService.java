package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Admin;

/**
 * <p>
 * 管理员表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IAdminService extends IService<Admin> {

    String getAdminName(Integer id);

}
