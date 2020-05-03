package com.zhuanbo.admin.api.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.dto.AdminDepositOrderDTO;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IDepositOrderService;

/**
 * @author: Jiekai Lin
 * @Description(描述): 充值记录表
 * @date: 2019/8/28 17:31
 */
@RestController
@RequestMapping("/mpmall/admin/user/depositOrder")
public class UserDepositOrderController {

    @Autowired
    IDepositOrderService iDepositOrderService;
    @Autowired
    IAdminService iAdminService;

    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestBody AdminDepositOrderDTO dto) throws Exception {
        return iDepositOrderService.list(dto);
    }

    @PostMapping("/exList")
    public Object exList(@LoginAdmin Integer adminId, @RequestBody AdminDepositOrderDTO dto) throws Exception {
        return iDepositOrderService.exList(dto);
    }

    @PostMapping("/exportExcel")
    public void exportExcel(@LoginAdmin Integer adminId, @RequestBody AdminDepositOrderDTO dto,HttpServletResponse response,HttpServletRequest request) throws Exception{
        // LogOperateUtil.log("数据统计", "充值记录导出", null, adminId.longValue(), 0);
         iDepositOrderService.exportExcel(dto,response, request);
    }

    /**
     * 退款
     */
    @GetMapping("/refund")
    @Transactional(rollbackFor = Exception.class)
    public Object enablePartnerDetail(@LoginAdmin Integer adminId, String orderNo) throws Exception {
        // LogOperateUtil.log("站外充值记录", "退款", orderNo, adminId.longValue(), 0);
        if(StringUtils.isBlank(orderNo)){
            return ResponseUtil.badArgumentValue();
        }
        DepositOrder depositOrder = iDepositOrderService.getOne(new QueryWrapper<DepositOrder>()
                .eq("order_no",orderNo)
                .eq("busi_type","02")
                .eq("trade_code","02")
        );
        if(null == depositOrder){
            return ResponseUtil.fail(10402, "境外充值订单不存在");
        }
//        if (OrderStatus.REFUND_SUCCESS.getId().equals(depositOrder.getOrderStatus())) {
//            return ResponseUtil.fail(10402, "订单已退款");
//        }
        if (!OrderStatus.SUCCESS.getId().equals(depositOrder.getOrderStatus())) {
            return ResponseUtil.fail(10402, "充值成功订单才能退款");
        }

//        depositOrder.setOrderStatus(OrderStatus.REFUND_SUCCESS.getId());
//        depositOrder.setOperatorId(adminId.longValue());
//        depositOrder.setOperator(iAdminService.getAdminName(adminId));
        if(iDepositOrderService.updateById(depositOrder)) {
            return ResponseUtil.ok();
        }else {
            return ResponseUtil.fail();
        }
    }
}