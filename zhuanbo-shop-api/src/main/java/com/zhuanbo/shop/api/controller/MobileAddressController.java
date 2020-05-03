package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.entity.Address;
import com.zhuanbo.service.service.IAddressService;
import com.zhuanbo.shop.api.dto.req.BaseParamsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告
 */
@RestController
@RequestMapping("/shop/mobile/address")
public class MobileAddressController {

    @Autowired
    private IAddressService addressService;

    /**
     *  列表
     *
     * @param userId
     * @param baseParamsDTO
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginUser Long userId,@RequestBody BaseParamsDTO baseParamsDTO) {

        baseParamsDTO.setSort("add_time");
        Page<Address> pageCond = new Page<>(baseParamsDTO.getPage(), baseParamsDTO.getLimit());
        IPage<Address> addressIPage = addressService.page(pageCond,  new QueryWrapper<Address>().orderByDesc("is_default").orderByDesc(baseParamsDTO.getSort())
                .eq("user_id", userId).eq("deleted", 0));
        addressService.fillRegion(addressIPage.getRecords());
        Map<String, Object> data = new HashMap<>();
        data.put("total", addressIPage.getTotal());
        data.put("items", addressIPage.getRecords());
        return ResponseUtil.ok(data);
    }


    /**
     * 增
     * @param address
     * @return
     */
    @PostMapping("/operate")
    public Object create(@LoginUser Long userId, @RequestBody Address address) {

        if (address.getName().length() > 16) {
            return ResponseUtil.result(40004);
        }

        LocalDateTime now = LocalDateTime.now();

        if (address.getId() == null) {// 添加
            address.setAddTime(now);
            address.setUpdateTime(now);
            address.setUserId(userId);
            address.setDeleted(0);
            if (addressService.count(new QueryWrapper<Address>().eq("user_id", userId).eq("deleted", 0)) == 0) {
                address.setIsDefault(1);
            }
            addressService.save(address);
        } else {// 修改
            address.setUserId(userId);
            address.setUpdateTime(now);
            addressService.updateById(address);
        }
        if (address.getIsDefault().equals(1)) {
            addressService.update(new Address(), new UpdateWrapper<Address>().set("is_default", 0).eq("user_id", userId).notIn("id", address.getId()));
        }
        return ResponseUtil.ok();
    }

    /**
     * 删
     * @return
     */
    @PostMapping("/delete")
    public Object delete(@LoginUser Long userId, @RequestBody Address address) {
        address = addressService.getById(address.getId());
        if (address == null) {
            return  ResponseUtil.badResult();
        }
        address.setDeleted(1);
        addressService.updateById(address);
        if (address.getIsDefault().equals(1)) {
            List<Address> list = addressService.list(new UpdateWrapper<Address>().eq("user_id", userId).eq("deleted", 0));
            if (!list.isEmpty()) {
                Address one = list.get(0);
                one.setIsDefault(1);
                addressService.updateById(one);
            }
        }
        return ResponseUtil.ok();
    }

    /**
     * 查
     * @param baseParamsDTO
     * @return
     */
    @PostMapping("/detail")
    public Object detail(@LoginUser Long userId, @RequestBody BaseParamsDTO baseParamsDTO) {
        Address address = addressService.getById(baseParamsDTO.getId());
        if (address == null) {
            return ResponseUtil.badResult();
        } else {
            addressService.fillRegion(Arrays.asList(address));
            return ResponseUtil.ok(address);
        }
    }

    /**
     * 改
     * @param address
     * @param address
     * @return
     */
    @PostMapping("/operate/default")
    public Object setDefaultAddress(@LoginUser Long userId, @RequestBody Address address) {

        if (address.getId() == null) {
            return ResponseUtil.badArgument();
        }
        addressService.update(new Address(), new UpdateWrapper<Address>()
                .set("is_default", 0).eq("user_id", userId).notIn("id", address.getId()));
        if(!addressService.updateById(address)){
            return ResponseUtil.result(11113);
        }
        return ResponseUtil.ok();
    }
}
