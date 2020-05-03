package com.zhuanbo.shop.api.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.vo.UserIncomeDetailsVO;
import com.zhuanbo.shop.api.dto.req.UserIncomeDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/shop/mobile/income/details")
@Slf4j
public class MobileUserIncomeDetailsController {

    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;

    @PostMapping("/list")
    public Object index(@LoginUser Long userId, @RequestBody UserIncomeDetailsDTO userIncomeDetailsDTO) {
        userIncomeDetailsDTO.setUserId(userId);
        if ("".equalsIgnoreCase(userIncomeDetailsDTO.getStartDate())) {
            userIncomeDetailsDTO.setStartDate(null);
        }
        if ("".equalsIgnoreCase(userIncomeDetailsDTO.getEndDate())) {
            userIncomeDetailsDTO.setEndDate(null);
        }
        Page<UserIncomeDetailsVO> pageCond = new Page<>(userIncomeDetailsDTO.getPage(), userIncomeDetailsDTO.getLimit());

        Map<String, Object> params = new HashMap<>();
        params.putAll(BeanUtil.beanToMap(userIncomeDetailsDTO));

        //获取数据
        IPage<UserIncomeDetailsVO> iPage = iUserIncomeDetailsService.listMap(pageCond, params);

        BigDecimal totalIncome = new BigDecimal(0);
        if (CollectionUtil.isNotEmpty(iPage.getRecords())) {
            for (UserIncomeDetailsVO vo : iPage.getRecords()) {
                switch (vo.getOperateType().intValue()) {
                    case 1:
                        totalIncome = totalIncome.add(vo.getOperateIncome());
                        break;
                    case 2:
                        totalIncome = totalIncome.subtract(vo.getOperateIncome());
                        break;
                    default:
                        break;
                }
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());
        data.put("items", iPage.getRecords());
        data.put("totalIncome", totalIncome);
        return ResponseUtil.ok(data);

    }

}
