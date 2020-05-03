package com.zhuanbo.shop.api.controller;


import com.zhuanbo.service.service.IBrandService;
import com.zhuanbo.shop.api.dto.req.BrandDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop/mobile/brand")
@Slf4j
public class MobileBrandController {

    @Autowired
    IBrandService iBrandService;

    @PostMapping("/list")
    public Object list(@RequestBody BrandDTO brandDTO) throws Exception {
        return iBrandService.listByMobile(brandDTO.getPage(), brandDTO.getLimit());
    }

    @PostMapping("/detail")
    public Object detail(@RequestBody BrandDTO brandDTO) throws Exception {
        return iBrandService.detailByMobile(brandDTO.getId());
    }
}
