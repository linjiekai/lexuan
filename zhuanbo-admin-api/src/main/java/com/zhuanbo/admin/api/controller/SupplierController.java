package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.dto.AdminSupplierDTO;
import com.zhuanbo.core.entity.Supplier;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.ISupplierService;
import com.zhuanbo.service.utils.LogOperateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 广告表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Slf4j
@RestController
@RequestMapping("/admin/supplier")
public class SupplierController {


    @Autowired
    private ISupplierService iSupplierService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_SUPPLIER_UPDATE = "LOCK_SUPPLIER_UPDATE_";
    @Autowired
    private IAdminService iAdminService;

    @PostMapping("/list")
    public Object listApi(@LoginAdmin Integer adminId, @RequestBody AdminSupplierDTO dto) {
        LogOperateUtil.log("供应商管理", "列表", null, adminId.longValue(), 0);
        Page<Supplier> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<Supplier> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(dto).ifPresent(s -> {
            Optional.ofNullable(s.getId()).ifPresent(id -> queryWrapper.eq("id", id));
            Optional.ofNullable(s.getName()).ifPresent(name -> queryWrapper.like("name", name));
        });
        IPage<Supplier> adIPage = iSupplierService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        data.put("items", adIPage.getRecords());
        return ResponseUtil.ok(data);
    }

    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody AdminSupplierDTO dto) {
        LogOperateUtil.log("供应商管理", "添加", null, adminId.longValue(), 0);
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        String supplier_code = iSeqIncrService.nextVal("supplier_code", 4, Align.LEFT);
        supplier.setCode(supplier_code);
        supplier.setOperator(iAdminService.getAdminName(adminId));
        supplier.setOperatorId(Long.valueOf(adminId));
        iSupplierService.save(supplier);
        return ResponseUtil.ok(supplier);
    }

    /**
     * 删
     *
     * @return
     */
    @PostMapping("/delete")
    public Object delete(@LoginAdmin Integer adminId, @RequestBody AdminSupplierDTO dto) {
        LogOperateUtil.log("供应商管理", "删除", null, adminId.longValue(), 0);
        iSupplierService.removeById(dto.getId());
        return ResponseUtil.ok();
    }


    @PostMapping("/detail")
    public Object detail(@LoginAdmin Integer adminId, @RequestBody AdminSupplierDTO dto) {
        LogOperateUtil.log("供应商管理", "详情", null, adminId.longValue(), 0);
        Supplier byId = iSupplierService.getById(dto.getId());
        return ResponseUtil.ok(byId == null ? "{}" : byId);
    }


    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminSupplierDTO dto) {
        LogOperateUtil.log("供应商管理", "更新", null, adminId.longValue(), 0);
        Long id = dto.getId();
        Supplier supplier = iSupplierService.getById(id);
        if (supplier == null) {
            log.error("|供应商管理|更新|供应商无效");
            throw new ShopException(11114);
        }
        supplier.setOrderConnectType(dto.getOrderConnectType());
        String key = LOCK_SUPPLIER_UPDATE + dto.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 30);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            supplier.setOperator(iAdminService.getAdminName(dto.getOperatorId().intValue()));
            supplier.setOperatorId(dto.getOperatorId().longValue());
            iSupplierService.updateById(supplier);
            return ResponseUtil.ok();
        } catch (Exception e) {
            throw e;
        } finally {
            if (b) {
                redissonLocker.unlock(key);
            }

        }
    }

}
