package com.zhuanbo.admin.api.controller;


import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.dto.AdminFinanceDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Jiekai Lin
 * @Description(描述): 财务管理
 * @date: 2019/9/10 11:59
 */
@RestController
@RequestMapping("/admin/finance")
public class FinanceController {

    /**
     * @Description(描述): 提现账户充值记录
     * @auther: Jack Lin
     * @param :[adminId]
     * @return :java.lang.Object
     * @date: 2019/9/10 12:02
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestBody AdminFinanceDTO dto) {

        return ResponseUtil.ok();
    }

    /**
     * @Description(描述): 提现账户充值记录
     * @auther: Jack Lin
     * @param :[adminId]
     * @return :java.lang.Object
     * @date: 2019/9/10 12:02
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId,@RequestBody AdminFinanceDTO dto) {

        return ResponseUtil.ok();
    }


}
