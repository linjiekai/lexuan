package com.zhuanbo.admin.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.CountryArea;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.ICountryAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  国家区域
 * </p>
 *
 * @author rome
 * @since 2019-01-22
 */
@RestController
@RequestMapping("/admin/mobile/area")
public class CountryAreaController {

    private final static Integer AREA_TYPE_0 = 0;// 普通
    private final static Integer AREA_TYPE_1 = 1;// 常用

    @Autowired
    private ICountryAreaService iCountryAreaService;

    @PostMapping("/list")
    public Object list(@RequestBody JSONObject jsonObject) {

        Integer page = jsonObject.getInteger("page");
        Integer limit = jsonObject.getInteger("limit");
        page = page == null ? 1 : page;
        limit = limit == null ? Integer.MAX_VALUE : limit;

        // 全部
        IPage<CountryArea> countryAreaIPage = iCountryAreaService.page(new Page<>(page, limit),
                new QueryWrapper<CountryArea>()
                .eq("type", AREA_TYPE_0)
                .orderByAsc("number"));

        Map<String, Object> finalMap = new HashMap<>();
        finalMap.put("normal", countryAreaIPage.getRecords());

        // 常用的
        List<CountryArea> list = iCountryAreaService.list(new QueryWrapper<CountryArea>()
                .orderByAsc("number").eq("type", AREA_TYPE_1));

        CountryArea countryArea = null;
        int index = 0;
        for (CountryArea c : list) {
            if (c.getNumber() != null && c.getNumber().equals(86)) {
                countryArea = c;
                break;
            }
            index++;
        }
        list.remove(index);
        list.add(0, countryArea);
        finalMap.put("common", list);

        return ResponseUtil.ok(finalMap);
    }
}
