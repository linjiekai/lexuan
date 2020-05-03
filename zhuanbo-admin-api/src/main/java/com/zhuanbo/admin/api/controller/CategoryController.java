package com.zhuanbo.admin.api.controller;


import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.dto.AdminCategoryDTO;
import com.zhuanbo.service.service.ICategoryService;
import com.zhuanbo.service.utils.LogOperateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商品类目表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 列表
     *
     * @param categoryDTO
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestBody AdminCategoryDTO categoryDTO) {
        return categoryService.queryList(categoryDTO);
    }

    /**
     * 增
     *
     * @param categoryDTO
     * @return
     */
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object create(@LoginAdmin Integer adminId, @RequestBody AdminCategoryDTO categoryDTO) {
        //记录日志表
        LogOperateUtil.log("商品分类管理", "添加", null, adminId.longValue(), 0);
        return categoryService.creatCategory(categoryDTO);

    }

    /**
     * 删
     *
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public Object delete(@LoginAdmin Integer adminId, @PathVariable("id") Long id) {
        LogOperateUtil.log("商品分类管理", "删除", String.valueOf(id), adminId.longValue(), 0);
        return categoryService.deleteCategory(id);
    }

    /**
     * 编辑
     *
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Long id) {
        return categoryService.detail(id);
    }

    /**
     * 改
     *
     * @param categoryDTO
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminCategoryDTO categoryDTO) {
        LogOperateUtil.log("商品分类管理", "更新", String.valueOf(categoryDTO.getId()), adminId.longValue(), 0);
        return categoryService.updateCategory(categoryDTO);
    }

}
