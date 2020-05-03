package com.zhuanbo.admin.api.controller;

import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.dto.AdminPlatformIncomeDetailsDTO;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IPlatformIncomeDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 平台收益明细
 *
 * @date 2020-03-26 15:27:24
 */
@Slf4j
@RestController
@RequestMapping("/admin/platform/income/details")
public class PlatformIncomeDetailsController {

    @Autowired
    private IPlatformIncomeDetailsService platformIncomeDetailsService;

    /**
     * 平台收益明细列表
     *
     * @param adminId
     * @param incomeDetailsDTO
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestBody AdminPlatformIncomeDetailsDTO incomeDetailsDTO) {
        log.info("|平台收益明细|明细列表|adminId:{}, 请求报文:{}", adminId, incomeDetailsDTO);
        Map<String, Object> dataMap = platformIncomeDetailsService.list(incomeDetailsDTO);
        return ResponseUtil.ok(dataMap);
    }
}
