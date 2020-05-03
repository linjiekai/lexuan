package com.zhuanbo.shop.api.controller.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.entity.Category;
import com.zhuanbo.core.entity.CategoryRelation;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.ShowCategory;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Transactional(rollbackFor = Exception.class)
@Slf4j
/**
 * 变更分类id
 * @author Administrator
 *
 */
public class CategoryController {
	
	@Autowired
    private IShowCategoryService iShowCategoryService;
	
	@Autowired
    private ICategoryService iCategoryService;
	
	@Autowired
    private ICategoryRelationService iCategoryRelationService;
	
	@Autowired
    private ISeqIncrService iSeqIncrService;
	
	@Autowired
    private IGoodsService iGoodsService;
	

	
    public Object change(HttpServletRequest request, HttpServletResponse response)  {
		List<Category> categoryList = iCategoryService.list(new QueryWrapper<Category>());
		List<ShowCategory> showCategoryList = iShowCategoryService.list(new QueryWrapper<ShowCategory>());
		
		Long id = null;
		String tempId = null;
		for (Category category : categoryList) {
			id = category.getId();
			if (id > 100000) {
				continue;
			}
			tempId = iSeqIncrService.paddingVal(category.getId() + "", 5, Align.LEFT);
			if (category.getLevel() == 1) {
				tempId = 1 + tempId;
			} else if (category.getLevel() == 2) {
				tempId = 2 + tempId;
			} else if (category.getLevel() == 3) {
				tempId = 3 + tempId;
			}
			
			iCategoryService.update(new Category(), new UpdateWrapper<Category>()
					.set("id", tempId)
					.eq("id", id)
					);
			iCategoryService.update(new Category(), new UpdateWrapper<Category>()
					.set("pid", tempId)
					.eq("pid", id)
					);
			
			if (category.getLevel() == 3) {
				iCategoryRelationService.update(new CategoryRelation(), new UpdateWrapper<CategoryRelation>()
						.set("category_id", tempId)
						.eq("category_id", id)
						);
				
				iGoodsService.update(new Goods(), new UpdateWrapper<Goods>()
						.set("category_id", tempId)
						.eq("category_id", id)
						);
			}
			
		}
		
		for (ShowCategory showCategory : showCategoryList) {
			
			id = showCategory.getId();
			if (id > 400000) {
				continue;
			}
			
			tempId = iSeqIncrService.paddingVal(showCategory.getId() + "", 5, Align.LEFT);
			if (showCategory.getLevel() == 1) {
				tempId = 4 + tempId;
			} else if (showCategory.getLevel() == 2) {
				tempId = 5 + tempId;
			} else if (showCategory.getLevel() == 3) {
				tempId = 6 + tempId;
			}
			
			iShowCategoryService.update(new ShowCategory(), new UpdateWrapper<ShowCategory>()
					.set("id", tempId)
					.eq("id", id)
					);
			iShowCategoryService.update(new ShowCategory(), new UpdateWrapper<ShowCategory>()
					.set("pid", tempId)
					.eq("pid", id)
					);
			
			if (showCategory.getLevel() == 3) {
				iCategoryRelationService.update(new CategoryRelation(), new UpdateWrapper<CategoryRelation>()
						.set("show_id", tempId)
						.eq("show_id", id)
						);
			}
			
		}
		log.info("数据变更成功..................................");
		return ResponseUtil.ok();
	}

}
