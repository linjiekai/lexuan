package com.zhuanbo.shop.api.controller;

import com.zhuanbo.service.service.IUserRealedService;
import com.zhuanbo.shop.api.dto.req.AdParamsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广告
 */
@RestController
@RequestMapping("/shop/mobile/user/realed")
public class MobileUserRealedController {

    @Autowired
    private IUserRealedService iUserRealedService;

    /**
     * 列表
     *
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody AdParamsDTO adParamsDTO) {
        return null;
    }

}
