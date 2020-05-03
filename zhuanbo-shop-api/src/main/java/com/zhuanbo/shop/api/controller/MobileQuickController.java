package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Quick;
import com.zhuanbo.service.service.IQuickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 快捷入口
 */
@RestController
@RequestMapping("/shop/mobile/quick")
public class MobileQuickController {

    @Autowired
    private IQuickService quickService;

    /**
     * 列表
     * @param
     * @return
     */
    @PostMapping("/list")
    public Object list() {
        //获取快捷入口记录信息，默认返回8条数据，数据状态status为1(0 下线 1 上线)，删除状态deleted(1 删除 0未删除)
    	List<Quick> list = quickService.list(new QueryWrapper<Quick>().eq("status", 1)
				.eq("deleted", ConstantsEnum.DELETED_0.integerValue()).orderByDesc("indexs").last(" limit 8"));
        //如果为空创建空list集合
    	if (null == list) {
    		list = new ArrayList<Quick>();
    	}
    	//创建返回map集合
    	Map<String, Object> data = new HashMap<>();
    	//把查询获取的数据添加到返回map
    	data.put("items", list);
    	//返回数据ßßßß
    	return ResponseUtil.ok(data);
    }


}
