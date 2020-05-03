package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.core.entity.Ship;
import com.zhuanbo.service.service.IShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/ship")
public class ShipController {

    @Autowired
    private IShipService iShipService;

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit,
                       @Sort @RequestParam(defaultValue = "id") String sort,
                       Ship ship) {

        IPage<Ship> pageCond = new Page<>(page, limit);
        QueryWrapper<Ship> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(sort);

        IPage<Ship> iPage = iShipService.page(pageCond, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", limit.equals(-1) ? iShipService.count(null) : iPage.getTotal());
        data.put("items", iPage.getRecords());
        return ResponseUtil.ok(data);
    }
}
