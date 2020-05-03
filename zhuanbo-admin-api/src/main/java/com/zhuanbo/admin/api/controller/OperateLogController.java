package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.core.entity.OperateLog;
import com.zhuanbo.service.service.IOperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 行政区域表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/log")
public class OperateLogController {

    @Autowired
    private IOperateLogService iOperateLogService;

    /**
     * 列表
     * @param page
     * @param limit
     * @param sort
     * @param operateLog
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "id") String sort,
                       OperateLog operateLog) {


        Page<OperateLog> operateLogPage = new Page<>(page, limit);
        QueryWrapper<OperateLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (operateLog != null) {
            if (operateLog.getOperateId() != null) {
                queryWrapper.eq("operate_id", operateLog.getOperateId());
            }
        }

        IPage<OperateLog> operateLogIPage = iOperateLogService.page(operateLogPage, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", operateLogIPage.getTotal());
        data.put("items", operateLogIPage.getRecords());
        return ResponseUtil.ok(data);
    }
}
