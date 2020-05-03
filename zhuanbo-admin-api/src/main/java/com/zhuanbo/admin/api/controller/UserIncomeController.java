package com.zhuanbo.admin.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginDealersAdmin;
import com.zhuanbo.core.constants.TradeCode;
import com.zhuanbo.core.dto.AdminPointDTO;
import com.zhuanbo.core.dto.AdminUserIncomeDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncome;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.vo.UserIncomePointVO;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserService;
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
 * @title: UserIncomeController
 * @description: TODO
 * @date 2020/4/22 22:34
 */
@Slf4j
@RestController
@RequestMapping("/admin/user/income")
public class UserIncomeController {

    @Resource
    private IUserIncomeService iUserIncomeService;
    @Resource
    private IUserService iUserService;

    /**
     * 积分充值
     *
     * @return
     */
    @PostMapping("/deposit/point")
    public Object depositPoint(@LoginDealersAdmin Integer adminId, @RequestBody AdminPointDTO adminPointDTO){
        log.info("|用户积分充值|操作人:{},请求报文:{}", adminId, adminPointDTO);
        adminPointDTO.setAdminId(adminId.longValue());
        // 获取用户信息
        Long userId = adminPointDTO.getUserId();
        String mobile = adminPointDTO.getMobile();
        String areaCode = adminPointDTO.getAreaCode();
        String authNo = adminPointDTO.getAuthNo();
        String remark = adminPointDTO.getRemark();
        User user = iUserService.getByIdOrMobileOrAuthNo(userId, mobile, areaCode, authNo);
        if (user == null) {
            throw new ShopException("用户信息不存在");
        }
        adminPointDTO.setTradeCode(TradeCode.DEPOSIT.getId());
        adminPointDTO.setUserId(user.getId());
        adminPointDTO.setRemark(remark);
        adminPointDTO.setAdminId(adminId.longValue());
        iUserIncomeService.depositPoint(adminPointDTO);
        return ResponseUtil.ok();
    }

    /**
     * 积分列表
     * @return
     */
    @PostMapping("/point/list")
    public Object pointList(@LoginDealersAdmin Integer adminId, @RequestBody AdminPointDTO adminPointDTO){
        log.info("|用户积分列表|操作人:{},请求报文:{}", adminId, adminPointDTO);
        IPage iPage = new Page(adminPointDTO.getPage(), adminPointDTO.getLimit());
        List<AdminUserIncomeDTO> userIncomeDTOS = iUserIncomeService.pagePointInfo(iPage, adminPointDTO);
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("total", iPage.getTotal());
        retMap.put("items", userIncomeDTOS);
        return ResponseUtil.ok(retMap);
    }

    /**
     * 我的积分
     * @param adminId
     * @return
     */
    @GetMapping("/get/point")
    public Object getPoint(@LoginDealersAdmin Integer adminId){
        log.info("|获取积分|操作人:{}", adminId);
        UserIncome userIncome = iUserIncomeService.getByUserId(adminId.longValue());
        UserIncomePointVO incomePointVO = new UserIncomePointVO();
        if (userIncome != null) {
            BeanUtil.copyProperties(userIncome, incomePointVO);
        }
        return ResponseUtil.ok(incomePointVO);
    }

}
