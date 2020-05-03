package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.entity.NotifyMsgPool;
import com.zhuanbo.service.service.INotifyMsgPoolService;
import com.zhuanbo.shop.api.dto.req.MsgPoolDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告
 */
@RestController
@RequestMapping("/shop/mobile/msg/pool")
public class MobileMsgPoolController {

    @Autowired
    private INotifyMsgPoolService iNotifyMsgPoolService;

    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody MsgPoolDTO msgPoolDTO) {

        Page<NotifyMsgPool> notifyMsgPoolPage = new Page<>(msgPoolDTO.getPage(), msgPoolDTO.getLimit());


        QueryWrapper<NotifyMsgPool> qw = new QueryWrapper<NotifyMsgPool>().orderByDesc("add_time");
        
        if (null != msgPoolDTO.getPtLevel()) {
        	qw.eq("pt_level", msgPoolDTO.getPtLevel());
        }
        
        IPage<NotifyMsgPool> notifyMsgPoolIPage= iNotifyMsgPoolService.page(notifyMsgPoolPage, qw);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("total", notifyMsgPoolIPage.getTotal());
        dataMap.put("items", notifyMsgPoolIPage.getRecords());

        return ResponseUtil.ok(dataMap);
    }

}
