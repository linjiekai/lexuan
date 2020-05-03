package com.zhuanbo.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.entity.BusinessEntrance;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IBusinessEntranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop/mobile/business/entrance")
public class MobileBusinessEntranceController {

    @Autowired
    private IBusinessEntranceService iBusinessEntranceService;

    /**
     * 列表
     * @return
     */
    @GetMapping("/list")
    public Object list() {
        QueryWrapper<BusinessEntrance> queryWrapper = new QueryWrapper<BusinessEntrance>();
        queryWrapper.eq("deleted",0);
        queryWrapper.eq("status",1);
        queryWrapper.orderByDesc  ("sequence_number");
        return ResponseUtil.ok(iBusinessEntranceService.list(queryWrapper));
    }

}
