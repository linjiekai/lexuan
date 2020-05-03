package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginDealersAdmin;
import com.zhuanbo.core.dto.UserPointDetailsDTO;
import com.zhuanbo.core.entity.AdminDealer;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdminDealerService;
import com.zhuanbo.service.service.IUserPointDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @title: UserPointDetails
 * @description: 用户积分明细
 * @date 2020/4/23 19:23
 */
@Slf4j
@RestController
@RequestMapping("/admin/user/point/details")
public class UserPointDetailsController {

    @Resource
    private IUserPointDetailsService iUserPointDetailsService;
    @Resource
    private IAdminDealerService iAdminDealerService;

    /**
     * 积分列表
     *
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginDealersAdmin Integer adminId, @RequestBody UserPointDetailsDTO userPointDetailsDTO){
        log.info("|积分交易明细|操作人:{},请求报文:{}", adminId, userPointDetailsDTO);
        IPage iPage = new Page(userPointDetailsDTO.getPage(), userPointDetailsDTO.getLimit());
        List<UserPointDetailsDTO> pointDetailsDTOList = iUserPointDetailsService.page(iPage, userPointDetailsDTO);
        Map<String, Object> retMap = new HashMap<>(2);
        retMap.put("total", iPage.getTotal());
        retMap.put("items", pointDetailsDTOList);
        return ResponseUtil.ok(retMap);
    }

    /**
     * 数据统计-积分数据
     *
     * @param adminId
     * @return
     */
    @GetMapping("/statistic/point")
    public Object statisticPoint(@LoginDealersAdmin Integer adminId){
        log.info("|数据统计|积分数据|操作人:{}", adminId);
        // 用户数据校验
        AdminDealer adminDealer = iAdminDealerService.getById(adminId);
        if (adminDealer == null) {
            throw new ShopException(71010);
        }
        Map<String, Object> dataMap = iUserPointDetailsService.statisticPoint();
        return ResponseUtil.ok(dataMap);
    }

    /**
     * 数据统计-积分数据
     *
     * @param adminId
     * @return
     */
    @PostMapping("/statistic/point/byday")
    public Object statisticPointByDay(@LoginDealersAdmin Integer adminId, @RequestBody UserPointDetailsDTO userPointDetailsDTO){
        log.info("|数据统计|积分日统计|操作人:{}, 接收到请求报文:{}", adminId, userPointDetailsDTO);
        // 用户数据校验
        AdminDealer adminDealer = iAdminDealerService.getById(adminId);
        if (adminDealer == null) {
            throw new ShopException(71010);
        }
        Map<String, Object> pointMap = iUserPointDetailsService.statisticPointByDay(userPointDetailsDTO);


        return ResponseUtil.ok(pointMap);
    }



}
