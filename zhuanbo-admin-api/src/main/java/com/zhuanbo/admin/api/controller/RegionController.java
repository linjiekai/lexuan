package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.core.entity.Region;
import com.zhuanbo.service.service.IRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 行政区域表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/region")
public class RegionController {

    @Autowired
    private IRegionService regionService;

    /**
     * 列表
     * @param page
     * @param limit
     * @param sort
     * @param region
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "id") String sort,
                       Region region) {


        IPage<Region> pageCond = new Page<>(page, limit);
        QueryWrapper<Region> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(sort);
        Optional.ofNullable(region).ifPresent(x ->{
            Optional.ofNullable(x.getName()).ifPresent(n -> queryWrapper.likeRight("name", n));
            Optional.ofNullable(x.getType()).ifPresent(n -> queryWrapper.eq("type", n));
            Optional.ofNullable(x.getCode()).ifPresent(n -> queryWrapper.eq("code", n));
            Optional.ofNullable(x.getPid()).ifPresent(n -> queryWrapper.eq("pid", n));
        });

        IPage<Region> adIPage = regionService.page(pageCond, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        data.put("items", adIPage.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 增
     * @param region
     * @param bindingResult
     * @return
     */
    /*@PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody @Valid Region region, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseUtil.badValidate(bindingResult);
        }

        createLock.lock();
        try {
            QueryWrapper<Region> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name",region.getName()).or().eq("code",region.getCode());
            List<Region> list = regionService.list(queryWrapper);
            if (!list.isEmpty()) {
                return ResponseUtil.fail(-1, "名称或编码已存在,请重新输入。<存在："
                        + list.stream().map(x -> "名称："+x.getName() + "、编号："+x.getCode()).collect(Collectors.joining("; ")) +">");
            }
            regionService.save(region);
        } finally {
            createLock.unlock();
        }
        return ResponseUtil.ok();
    }*/


    /**
     * 删
     * @param id
     * @return
     */
    /*@PostMapping("/delete/{id}")
    public Object delete(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        regionService.deleteLevelByLevel(id);
        return ResponseUtil.ok();
    }*/

    /**
     * 查
     * @param id
     * @return
     */
    /*@GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        Region region = regionService.getById(id);
        if (region == null) {
            return ResponseUtil.badResult();
        } else {
            return ResponseUtil.ok(region);
        }
    }*/

    /**
     * 改
     * @param region
     * @param bindingResult
     * @return
     */
    /*@PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody @Valid Region region, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseUtil.badValidate(bindingResult);
        }
        if (region.getId() == null) {
            return ResponseUtil.badArgument();
        }

        updateLock.lock();

        try {
            QueryWrapper<Region> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name",region.getName()).or().eq("code",region.getCode());
            List<Region> list = regionService.list(queryWrapper);
            // 是否存在重复的名称或编码
            boolean isExist = false;
            StringBuffer stringBuffer = null;

            for (Region r : list) {
                System.out.println(r.getCode());
                System.out.println(r.getName());
                if ((region.getCode().equals(r.getCode()) || region.getName().equals(r.getName())) && !region.getId().equals(r.getId())) {
                    if (!isExist) {
                        stringBuffer = new StringBuffer();
                    }
                    isExist = true;
                    stringBuffer.append("姓名：").append(r.getName()).append("、编号：").append(r.getCode()).append("; ");
                }
            }
            if (isExist) {
                return ResponseUtil.fail(-1, "名称或编码已存在,请重新输入。<存在：" + stringBuffer .toString() + ">");
            }

        } finally {
            updateLock.unlock();
        }
        return regionService.updateById(region) ? ResponseUtil.ok() : ResponseUtil.fail(502, "更新失败，请重新载入数据后再操作");
    }*/

    /**
     * 根据父ID获取子、孙数据
     * @param adminId
     * @param pid 父ID
     * @param level 0：只获取子级数组，1：获取子、孙等全部后代数据
     * @return
     */
    /*@GetMapping("/children")
    public Object children(@LoginAdmin Integer adminId,
                           @RequestParam(defaultValue = "0") Integer pid,
                           @RequestParam(defaultValue = "0") Integer level) {
        return ResponseUtil.ok(regionService.children(pid, level));
    }*/
}
