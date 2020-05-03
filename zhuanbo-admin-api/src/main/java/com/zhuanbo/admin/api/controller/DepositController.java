package com.zhuanbo.admin.api.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.dto.MobileDepositOrderDTO;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/deposit")
@Slf4j
public class DepositController {

    @Autowired
    private IDepositOrderService iDepositOrderService;


    /**
     * 分页查询充值订单
     *
     * @param uid
     * @param depositOrderDTO
     * @return
     */
    @GetMapping("/page")
    public Object page(@LoginUser Long uid, @RequestBody MobileDepositOrderDTO depositOrderDTO) {
        log.info("|充值订单列表|分页查询|用户id：{}", uid);
        QueryWrapper<DepositOrder> queryWrapper = new QueryWrapper<>();
        IPage iPage = new Page(depositOrderDTO.getPage(), depositOrderDTO.getLimit());
        queryWrapper.eq("merc_id", depositOrderDTO.getMercId());
        queryWrapper.eq("user_id", uid);
        queryWrapper.orderByDesc("add_time");
        IPage depositOrderPage = iDepositOrderService.page(iPage, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", depositOrderPage.getTotal());
        data.put("items", depositOrderPage.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 获取充值订单
     *
     * @param uid
     * @param depositOrderDTO
     * @return
     */
    @PostMapping("/get/info")
    public Object getInfo(@RequestBody MobileDepositOrderDTO depositOrderDTO) {
        log.info("|获取充值订单信息|请求报文depositOrderDTO:{}", depositOrderDTO);
        QueryWrapper<DepositOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merc_id", depositOrderDTO.getMercId());
        queryWrapper.eq("deposit_no", depositOrderDTO.getDepositNo());
        DepositOrder depositOrder = iDepositOrderService.getOne(queryWrapper);
        
        if (null == depositOrder) {
        	return ResponseUtil.fail(10060);
        }
        
        Integer buyTypeCount = iDepositOrderService.count(new QueryWrapper<DepositOrder>()
        		.eq("user_id", depositOrder.getUserId())
        		.eq("busi_type", depositOrder.getBusiType())
        		.eq("trade_code", depositOrder.getTradeCode())
        		.notIn("order_status", "W", "C")
        		);
        
        Map<String, Object> data = JSON.parseObject(JSON.toJSONString(depositOrder), Map.class);
        data.put("buyTypeCount", buyTypeCount);
        return ResponseUtil.ok(data);
    }
}
