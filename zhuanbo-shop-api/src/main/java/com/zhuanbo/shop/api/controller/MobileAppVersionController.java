package com.zhuanbo.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.AppVersion;
import com.zhuanbo.service.service.IAppVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop/mobile/app/version")
public class MobileAppVersionController {

    @Autowired
    private IAppVersionService iAppVersionService;

    @Transactional
    @PostMapping("/check")
    public Object check(@RequestBody AppVersion appVersion) {

        List<AppVersion> list = iAppVersionService.list(new QueryWrapper<AppVersion>().eq("platform", appVersion.getPlatform())
                .eq("sys_cnl", appVersion.getSysCnl()).eq("status", 1)
                .eq("deleted", ConstantsEnum.DELETED_0.integerValue()).orderByDesc("eff_time").orderByDesc("id").last("limit 1"));
        if (!list.isEmpty()) {
            return ResponseUtil.ok(list.get(0));
        }
        return ResponseUtil.ok();
    }

}
