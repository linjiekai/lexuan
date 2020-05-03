package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.dto.DictionaryDTO;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IDictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/dictionary")
@Slf4j
public class DictionaryController {

    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private IAdminService iAdminService;

    /**
     * 列表
     * @param dictionaryDTO
     * @return
     */
    @PostMapping("/page")
    public Object page(@LoginAdmin Integer adminId, @RequestBody DictionaryDTO dictionaryDTO) {
        log.info("|字典表分页查询|adminId:{}, 请求报文:{}", adminId, dictionaryDTO);
        IPage<Dictionary> dictionaryIPage = dictionaryService.pageInfo(dictionaryDTO);
        Map<String, Object> data = new HashMap<>();
        data.put("total", dictionaryIPage.getTotal());
        data.put("items", dictionaryIPage.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 字典详情
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Long id) {
        log.info("|字典表修改|adminId:{}, 字典id:{}", adminId, id);
        Dictionary dictionary = dictionaryService.getById(id);
        return ResponseUtil.ok(dictionary);
    }

    /**
     * 字典编辑
     *
     * @param adminId
     * @param dictionaryDTO
     * @return
     */
    @PostMapping("/edit")
    public Object edit(@LoginAdmin Integer adminId, @RequestBody DictionaryDTO dictionaryDTO) {
        log.info("|字典表修改|adminId:{}, 请求报文:{}", adminId, dictionaryDTO);
        boolean editFlag = dictionaryService.edit(adminId, dictionaryDTO);
        return ResponseUtil.ok(editFlag);
    }

    /**
     * 提现字典列表
     * @param dictionaryDTO
     * @return
     */
    @PostMapping("/withdr/page")
    public Object withdrPage(@LoginAdmin Integer adminId, @RequestBody DictionaryDTO dictionaryDTO) {
        log.info("|提现字典分页查询|adminId:{}, 请求报文:{}", adminId, dictionaryDTO);
        IPage<Dictionary> dictionaryIPage = dictionaryService.pageWithdr(dictionaryDTO);
        Map<String, Object> data = new HashMap<>();
        data.put("total", dictionaryIPage.getTotal());
        data.put("items", dictionaryIPage.getRecords());
        return ResponseUtil.ok(data);
    }

}
