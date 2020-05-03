package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.AdminDealer;

/**
 * <p>
 * 管理员表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IAdminDealerService extends IService<AdminDealer> {

    String getAdminName(Integer id);

}
