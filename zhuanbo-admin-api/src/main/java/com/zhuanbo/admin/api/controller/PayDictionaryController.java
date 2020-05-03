package com.zhuanbo.admin.api.controller;

import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.dto.PayDictionaryDTO;
import com.zhuanbo.service.service.IPayDictionaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @title: PayDictionaryController
 * @date 2020/4/1 14:01
 */
@RestController
@RequestMapping("/admin/pay/dictionary")
public class PayDictionaryController {

    @Resource
    private IPayDictionaryService iPayDictionaryService;

    /**
     * pay字典分页查询
     * @param adminId
     * @param payDictionaryDTO
     * @return
     */
    @GetMapping("/page")
    public Object page(@LoginAdmin Integer adminId, PayDictionaryDTO payDictionaryDTO) {
        return iPayDictionaryService.page(payDictionaryDTO);
    }

    /**
     * pay字典更新
     * @param adminId
     * @param payDictionaryDTO
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody PayDictionaryDTO payDictionaryDTO) {
        payDictionaryDTO.setAdminId(adminId);
        return iPayDictionaryService.update(payDictionaryDTO);
    }
}
