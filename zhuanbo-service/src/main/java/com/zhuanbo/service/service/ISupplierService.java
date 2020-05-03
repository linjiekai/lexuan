package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Supplier;

import java.util.List;

/**
 * <p>
 * 供应商表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-08-12
 */
public interface ISupplierService extends IService<Supplier> {

    /**
     * 获取多个供应商的名称
     * @param codes
     * @return
     */
    List<String> getNamesByCodes(List<String> codes);
}
