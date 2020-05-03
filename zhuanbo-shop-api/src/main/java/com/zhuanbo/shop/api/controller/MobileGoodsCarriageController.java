package com.zhuanbo.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.entity.GoodsCarriage;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IGoodsCarriageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运费模板
 */
@RestController
@RequestMapping("/shop/mobile/gc")
public class MobileGoodsCarriageController {

    @Autowired
    private IGoodsCarriageService iGoodsCarriageService;

    /**
     * 列表
     * @return
     */
    @GetMapping("/list")
    public Object list(Integer id) {
        if(null != id){
            return ResponseUtil.ok(iGoodsCarriageService.getById(id));
        }else{
            return ResponseUtil.ok(iGoodsCarriageService.list(new QueryWrapper<GoodsCarriage>()));
        }
    }

}
