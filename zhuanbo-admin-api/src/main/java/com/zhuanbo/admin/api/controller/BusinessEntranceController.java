package com.zhuanbo.admin.api.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.dto.BusinessEntranceDTO;
import com.zhuanbo.core.entity.BusinessEntrance;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IBusinessEntranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/business/entrance")
public class BusinessEntranceController {

    @Autowired
    private IBusinessEntranceService iBusinessEntranceService;
    @Autowired
    private IAdminService iAdminService;

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer limit) throws Exception{
        Page<BusinessEntrance> iPage = new Page<>(page, limit);
        QueryWrapper<BusinessEntrance> queryWrapper = new QueryWrapper<BusinessEntrance>();
        queryWrapper.eq("deleted",0);
        queryWrapper.orderByDesc  ("sequence_number");
        IPage<BusinessEntrance> list = iBusinessEntranceService.page(iPage,queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", list.getTotal());
        data.put("items", list.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 增
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody BusinessEntranceDTO dto) throws Exception {
        BusinessEntrance businessEntrance = new BusinessEntrance();
        BeanUtil.copyProperties(dto,businessEntrance);
        businessEntrance.setOperator(iAdminService.getAdminName(adminId));
        boolean result = iBusinessEntranceService.save(businessEntrance);
        if(result){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.fail();
        }
    }


    /**
     * 删
     *
     * @return
     */
    @GetMapping("/delete")
    public Object delete(@LoginAdmin Integer adminId, @RequestParam("id") Integer id) throws Exception{
        BusinessEntrance businessEntrance = iBusinessEntranceService.getById(id);
        if (businessEntrance == null) {
            return ResponseUtil.badResult();
        }
        businessEntrance.setOperator(iAdminService.getAdminName(adminId));
        businessEntrance.setDeleted(true);
        boolean result = iBusinessEntranceService.updateById(businessEntrance);
        if(result){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.fail();
        }
    }

    /**
     * 查
     *
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public Object detail(@LoginAdmin Integer adminId, @RequestParam("id") Integer id) {
        BusinessEntrance businessEntrance = iBusinessEntranceService.getById(id);
        if (businessEntrance == null) {
            return ResponseUtil.badResult();
        } else {
            BusinessEntranceDTO dto = new BusinessEntranceDTO();
            BeanUtil.copyProperties(businessEntrance,dto);
            return ResponseUtil.ok(dto);
        }
    }

    /**
     * 改
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody BusinessEntranceDTO dto) throws Exception {
        BusinessEntrance businessEntrance = iBusinessEntranceService.getById(dto.getId());
        if (businessEntrance == null) {
            return ResponseUtil.badResult();
        }
        BeanUtil.copyProperties(dto,businessEntrance);
        businessEntrance.setOperator(iAdminService.getAdminName(adminId));
        boolean result = iBusinessEntranceService.updateById(businessEntrance);
        if(result){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.fail();
        }
    }

}
