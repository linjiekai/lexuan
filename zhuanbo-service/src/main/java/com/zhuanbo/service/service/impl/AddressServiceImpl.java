package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Address;
import com.zhuanbo.core.entity.Region;
import com.zhuanbo.service.mapper.AddressMapper;
import com.zhuanbo.service.service.IAddressService;
import com.zhuanbo.service.service.IRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 收货地址表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    @Autowired
    IRegionService regionService;

    public String detailedAddress(Address Address) {
        Integer provinceId = Address.getProvinceId();
        Integer cityId = Address.getCityId();
        Integer areaId = Address.getAreaId();
        String provinceName = regionService.getById(provinceId).getName();
        String cityName = regionService.getById(cityId).getName();
        String areaName = regionService.getById(areaId).getName();
        String fullRegion = provinceName + " " + cityName + " " + areaName;
        return fullRegion + " " + Address.getAddress();
    }

    @Override
    public void fillRegion(Collection<Address> address) {

        List<Integer> idList = address.stream().flatMap(i -> Stream.of(i.getProvinceId(), i.getCityId(), i.getAreaId())).collect(Collectors.toList());
        if (!idList.isEmpty()) {

            QueryWrapper<Region> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", idList);
            List<Region> regionList = regionService.list(queryWrapper);

            if (!regionList.isEmpty()) {

                Map<Integer, String> regionMap = regionList.stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));
                for (Address i : address) {
                    i.setProvinceName(regionMap.get(i.getProvinceId()));
                    i.setCityName(regionMap.get(i.getCityId()));
                    i.setAreaName(regionMap.get(i.getAreaId()));
                }
            }
        }
    }
}
