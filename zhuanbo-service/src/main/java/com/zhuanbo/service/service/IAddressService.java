package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Address;

import java.util.Collection;

/**
 * <p>
 * 收货地址表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IAddressService extends IService<Address> {
    public String detailedAddress(Address Address);

    /**
     * 地址补上省、市、区名称
     * @param address
     */
    public void fillRegion(Collection<Address> address);
}
