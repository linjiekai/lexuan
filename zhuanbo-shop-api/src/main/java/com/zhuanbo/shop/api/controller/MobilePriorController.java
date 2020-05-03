package com.zhuanbo.shop.api.controller;

import com.google.common.collect.Lists;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.service.vo.AdVO;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优享
 */
@RestController
@RequestMapping("/shop/mobile/prior")
public class MobilePriorController {


    @GetMapping("/index")
    public Object index() throws  Exception{
        //优享广告
        List<AdVO> list = (List<AdVO>)RedisUtil.get(ConstantsEnum.REDIS_PRIOR_ADS.stringValue());
        Map<String, Object> data = new HashMap<>();
        data.put("ads", CollectionUtils.isEmpty(list)? Lists.newArrayList():list);
        return ResponseUtil.ok(data);
    }
}
