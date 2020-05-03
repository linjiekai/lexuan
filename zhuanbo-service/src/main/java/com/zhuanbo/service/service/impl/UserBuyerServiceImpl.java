package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Address;
import com.zhuanbo.core.entity.UserBuyer;
import com.zhuanbo.service.mapper.UserBuyerMapper;
import com.zhuanbo.service.service.IAddressService;
import com.zhuanbo.service.service.IUserBuyerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 用户订购人信息表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-09-02
 */
@Service
public class UserBuyerServiceImpl extends ServiceImpl<UserBuyerMapper, UserBuyer> implements IUserBuyerService {

    @Autowired
    private IAddressService iAddressService;

    @Override
    public boolean checkAddressName(Long userBuyerId, Integer addressId) {
        if (userBuyerId == null || addressId == null) {
            return false;
        }
        UserBuyer userBuyer = getById(userBuyerId);
        if (userBuyer == null) {
            return false;
        }
        Address address = iAddressService.getOne(new QueryWrapper<Address>().select("name").eq("id", addressId));
        if (address == null) {
            return false;
        }
        if (userBuyer.getName().equals(address.getName())) {
            return true;
        }
        return false;
    }

    @Override
    public IPage<UserBuyer> pageCustom(Page<UserBuyer> page, Map<String, Object> params) {
        QueryWrapper<UserBuyer> queryWrapper = new QueryWrapper<>();
        Long userId = (Long) params.get("userId");
        String name = (String) params.get("name");
        queryWrapper.select("id","name","card_type","card_no_abbr","add_time");
        queryWrapper.orderByDesc("id");
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        return page(page, queryWrapper);
    }
}
