package com.zhuanbo.shop.api.controller;

import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdService;
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
@RequestMapping("/shop/mobile/ad")
public class MobileAdController {

    @Autowired
    private IAdService adService;

    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody AdParamsDTO adParamsDTO) {
        //获取广告数据
        return ResponseUtil.ok(adService.getAdList(adParamsDTO.getPosition(),adParamsDTO.getPlatform()));
    }

}
