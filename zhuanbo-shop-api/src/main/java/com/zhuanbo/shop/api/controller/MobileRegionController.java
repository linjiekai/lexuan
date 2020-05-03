package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.entity.Region;
import com.zhuanbo.service.service.IRegionService;
import com.zhuanbo.shop.api.dto.req.BaseParamsDTO;
import com.zhuanbo.shop.api.dto.resp.RegionTreeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 区域
 */
@RestController
@RequestMapping("/shop/mobile/region")
public class MobileRegionController {

    @Autowired
    private IRegionService regionService;

    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody BaseParamsDTO baseParamsDTO) {

        Page<Region> pageCond = new Page<>(baseParamsDTO.getPage(), baseParamsDTO.getLimit());
        IPage<Region> iPage = regionService.page(pageCond, new QueryWrapper<Region>().lt("type", 4));// 省市区

        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());

        if (iPage.getRecords().size() > 0) {
            List<Region> records = iPage.getRecords();

            List<RegionTreeDTO> regionTreeDTOList = records.stream().map(x -> new RegionTreeDTO(x.getId(), x.getName(), x.getPid())).collect(Collectors.toList());
            // 转成树
            data.put("items", toTree(regionTreeDTOList));
        } else {
            data.put("items", new ArrayList<>(0));
        }
        return ResponseUtil.ok(data);
    }

    public static List<RegionTreeDTO> toTree(List<RegionTreeDTO> list){

        Map<Integer, RegionTreeDTO> mapTmp = new HashMap<>();
        for (RegionTreeDTO current : list) {
            mapTmp.put(current.getId(), current);
        }

        List<RegionTreeDTO> finalList = new ArrayList<>();

        mapTmp.forEach((k, v) -> {
            if(v.getPId() == null || v.getPId().equals(0)) {
                finalList.add(v);
            } else {
                mapTmp.get(v.getPId()).getChildren().add(v);
            }
        });
        return finalList;
    }
}
