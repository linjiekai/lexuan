package com.zhuanbo.shop.api.controller;


import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.entity.Quick;
import com.zhuanbo.core.entity.ShowCategory;
import com.zhuanbo.service.service.IQuickService;
import com.zhuanbo.service.service.IShowCategoryService;
import com.zhuanbo.shop.api.dto.req.ShowCategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类别表
 */
@RestController
@RequestMapping("/shop/mobile/show/category")
public class MobileShowCategoryController {

    @Autowired
    private IShowCategoryService iShowCategoryService;
    
    @Autowired
    private IQuickService iQuickService;

    /**
     * 展示分类(树)
     * @return
     * @throws Exception 
     */
    @PostMapping("/index")
    public Object index(@RequestBody ShowCategoryDTO showCategoryDTO) throws Exception {
    	
    	List<ShowCategory> childrenList = iShowCategoryService.getChildrenCache(showCategoryDTO.getPid());
    	if (null == childrenList) {
    		childrenList = new ArrayList<ShowCategory>();
    	}
    	
        Map<String, Object> data = new HashMap<>();
        data.put("items", childrenList);
        
        return ResponseUtil.ok(data);
    }

    /**
     * 列表
     * @param showCategoryDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/list")
    public Object list(@RequestBody ShowCategoryDTO showCategoryDTO) throws Exception {
    	
    	ShowCategory parentShowCategory = null;
    	if (showCategoryDTO.getType() == 0) {
    		parentShowCategory = iShowCategoryService.getParentRootCache(showCategoryDTO.getId());
    	} else if (showCategoryDTO.getType() == 1){
    		Quick quick = iQuickService.getById(showCategoryDTO.getId());
    		parentShowCategory = iShowCategoryService.getParentRootCache(quick.getCategoryId());
    	}
    	
    	if (null == parentShowCategory || parentShowCategory.getStatus() != 1 || parentShowCategory.getDeleted() == 1) {
    		return ResponseUtil.fail(41003);
    	}
    	
    	List<ShowCategory> dataList = iShowCategoryService.getChildrenCache(parentShowCategory.getId());

    	parentShowCategory.setChildrenList(new ArrayList<ShowCategory>());
    	
    	for (ShowCategory showCategory : dataList) {
    		if (null != showCategory.getChildrenList() && showCategory.getChildrenList().size() > 0) {
    			parentShowCategory.getChildrenList().addAll(showCategory.getChildrenList());
    		}
    	}
		
        Map<String, Object> data = new HashMap<>();
        data.put("showCategory", parentShowCategory);
        
        return ResponseUtil.ok(data);
    }

}
