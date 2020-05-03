package com.zhuanbo.shop.api.controller;

import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.dto.MobileGoodsDisplayDTO;
import com.zhuanbo.service.service.IGoodsDisplayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop/mobile/goodsDisplay")
public class MobileGoodsDisplayController {

    @Autowired
    private IGoodsDisplayService iGoodsDisplayService;

    /**
     * 列表
     *
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginUser Long id) {

        return iGoodsDisplayService.listByUserId(id);
    }

    /**
     * 添加首推商品
     *
     * @return
     */
    @PostMapping("/addPrimaryGoods")
    public Object addPrimaryGoods(@LoginUser Long id, @RequestBody MobileGoodsDisplayDTO dto) {
        dto.setUserId(id);
        return iGoodsDisplayService.addPrimaryGoods(dto);
    }


}
