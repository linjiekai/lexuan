package com.zhuanbo.admin.api.controller;

import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.dto.AdminShowCategoryDTO;
import com.zhuanbo.service.service.IShowCategoryService;
import com.zhuanbo.service.utils.LogOperateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 展示类目表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/showCategory")
@Slf4j
public class ShowCateoryController {

    @Autowired
    public IShowCategoryService iShowCategoryService;

    /**
     * 列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestBody AdminShowCategoryDTO dto)  throws Exception{
        return iShowCategoryService.queryList(dto);
    }

    /**
     * 增
     *
     * @param dto
     * @return
     * @throws Exception 
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody AdminShowCategoryDTO dto) throws Exception {
        //记录日志表
        LogOperateUtil.log("展示分类管理", "添加", null, adminId.longValue(), 0);
        return iShowCategoryService.creatShowCategory(dto);

    }

    /**
     * 删
     *
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public Object delete(@LoginAdmin Integer adminId, @PathVariable("id") Long id)  throws Exception{
        LogOperateUtil.log("展示分类管理", "删除", String.valueOf(id), adminId.longValue(), 0);
        return iShowCategoryService.deleteShowCategory(id);
    }

    /**
     * 改
     *
     * @param dto
     * @return
     * @throws Exception 
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminShowCategoryDTO dto) throws Exception {
        LogOperateUtil.log("展示分类管理", "更新", String.valueOf(dto.getId()), adminId.longValue(), 0);
        return iShowCategoryService.updateShowCategory(dto);
    }

    /**
     * 编辑
     *
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Long id)  throws Exception{
        return iShowCategoryService.detail(id);
    }

    /**
     * 刷新缓存
     *
     * @return
     */
    @GetMapping("/refreshAllCache")
    public Object refreshCache(@LoginAdmin Integer adminId) throws Exception {
        LogOperateUtil.log("展示分类管理", "刷新缓存", null, adminId.longValue(), 0);
        iShowCategoryService.refreshAllCache();
        return ResponseUtil.ok();
    }
}
