package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.AppVersion;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IAppVersionService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 广告表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/app")
public class AppVersionController {

    @Autowired
    private IAppVersionService iAppVersionService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_APPVERSION_UPDATE = "lock_appversion_update_";

    /**
     * 列表
     *
     * @param adminId
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       AppVersion appVersion) {

        IPage<AppVersion> iPage = new Page<>(page, limit);
        QueryWrapper<AppVersion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        queryWrapper.orderByDesc("id");
        if (appVersion.getId() != null) {
            queryWrapper.eq("id", appVersion.getId());
        }
        if (StringUtils.isNotBlank(appVersion.getPlatform())) {
            queryWrapper.eq("platform", appVersion.getPlatform());
        } else {
            queryWrapper.eq("platform", ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        }

        IPage<AppVersion> list = iAppVersionService.page(iPage, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", list.getTotal());
        data.put("items", list.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 添加
     *
     * @param adminId
     * @param appVersion
     * @return
     */
    @Transactional
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody AppVersion appVersion) {
        LogOperateUtil.log("App管理", "创建", null, adminId.longValue(), 0);

        if (StringUtils.isBlank(appVersion.getSysCnl()) || StringUtils.isBlank(appVersion.getVersion()) || StringUtils.isBlank(appVersion.getEffVersion())
                || appVersion.getEffTime() == null || StringUtils.isBlank(appVersion.getDownloadUrl()) || StringUtils.isBlank(appVersion.getPlatform())) {
            return ResponseUtil.fail("11111", "缺少参数：sysCnl或version或effVersion或effTime或downloadUrl或platform");
        }
        LocalDateTime now = LocalDateTime.now();
        // 生效状态
        if (appVersion.getEffTime().isBefore(now)) {
            appVersion.setStatus(1);// 生
            // 生效中的状态记录只有1条
            iAppVersionService.update(new AppVersion(), new UpdateWrapper<AppVersion>().set("status", 2).eq("platform", appVersion.getPlatform()).eq("sys_cnl", appVersion.getSysCnl()).eq("status", 1));
        } else {
            appVersion.setStatus(0);// 不生
        }
        appVersion.setRedirectUrl(appVersion.getDownloadUrl());// 目前一样
        appVersion.setVersionDate(DateUtil.toyyyy_MM_dd(now));
        appVersion.setVersionTime(DateUtil.toHH_mm_ss(now));
        appVersion.setAddTime(now);
        appVersion.setUpdateTime(now);
        appVersion.setOperator(iAdminService.getAdminName(adminId));
        iAppVersionService.save(appVersion);

        AppVersion appVersion1 = new AppVersion();
        appVersion1.setId(appVersion.getId());
        return list(adminId, 1, 1, appVersion1);
    }

    /**
     * 详情
     *
     * @param adminId
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        return ResponseUtil.ok(iAppVersionService.getById(id));
    }

    /**
     * 更新
     *
     * @param adminId
     * @param appVersion
     * @return
     */
    @Transactional
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AppVersion appVersion) {
        LogOperateUtil.log("App管理", "更新", String.valueOf(appVersion.getId()), adminId.longValue(), 0);
        if (StringUtils.isBlank(appVersion.getSysCnl()) || StringUtils.isBlank(appVersion.getVersion()) || StringUtils.isBlank(appVersion.getEffVersion())
                || appVersion.getEffTime() == null || StringUtils.isBlank(appVersion.getDownloadUrl())
                || StringUtils.isBlank(appVersion.getPlatform()) || appVersion.getId() == null) {
            return ResponseUtil.fail("11111", "缺少参数：id或sysCnl或version或effVersion或effTime或downloadUrl或platform");
        }
        String lockKey = LOCK_APPVERSION_UPDATE + appVersion.getId();
        boolean b = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 10, 30);
        if (!b) {
            return ResponseUtil.result(22001);
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            // 生效状态
            if (appVersion.getEffTime().isBefore(now)) {
                appVersion.setStatus(1);// 生
                // 生效中的状态记录只有1条
                iAppVersionService.update(new AppVersion(), new UpdateWrapper<AppVersion>().set("status", 2)
                        .eq("platform", appVersion.getPlatform()).eq("sys_cnl", appVersion.getSysCnl())
                        .eq("status", 1).notIn("id", appVersion.getId()));
            } else {
                appVersion.setStatus(0);// 不生
            }
            appVersion.setRedirectUrl(appVersion.getDownloadUrl());// 目前一样
            appVersion.setVersionDate(DateUtil.toyyyy_MM_dd(now));
            appVersion.setVersionTime(DateUtil.toHH_mm_ss(now));
            appVersion.setUpdateTime(now);
            appVersion.setOperator(iAdminService.getAdminName(adminId));
            iAppVersionService.updateById(appVersion);

            AppVersion appVersion1 = new AppVersion();
            appVersion1.setId(appVersion.getId());
            return list(adminId, 1, 1, appVersion1);
        } catch (Exception e) {
            throw e;
        } finally {
            if (b) {
                redissonLocker.unlock(lockKey);
            }
        }

    }

    @PostMapping("/del")
    public Object del(@LoginAdmin Integer adminId, @RequestBody AppVersion appVersion) {
        LogOperateUtil.log("App管理", "删除", String.valueOf(appVersion.getId()), adminId.longValue(), 0);
        if (appVersion.getId() == null) {
            return ResponseUtil.fail("11111", "缺少参数：id");
        }
        appVersion = iAppVersionService.getById(appVersion.getId());
        if (appVersion != null) {
            appVersion.setUpdateTime(LocalDateTime.now());
            appVersion.setDeleted(ConstantsEnum.DELETED_1.integerValue());
            iAppVersionService.updateById(appVersion);
        }
        return ResponseUtil.ok();
    }
}
