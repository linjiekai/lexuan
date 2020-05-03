package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.dto.WxUserDTO;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/wx/user")
public class WxUserListController {

    @Autowired
    private IUserService userService;

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer limit, WxUserDTO dto) throws Exception{
        Page<WxUserDTO> pageCond = new Page<>(page, limit);
        if(StringUtils.isNotBlank(dto.getMobile())){
            dto.setMobile("%"+dto.getMobile()+"%");
        }
        if(StringUtils.isNotBlank(dto.getNickname())){
            dto.setNickname("%"+dto.getNickname()+"%");
        }
        if(StringUtils.isNotBlank(dto.getWxnickname())){
            dto.setWxnickname("%"+dto.getWxnickname()+"%");
        }
        IPage<WxUserDTO> adIPage = userService.wxUserList(pageCond, dto);

        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        data.put("items", adIPage.getRecords());
        return ResponseUtil.ok(data);
    }


}
