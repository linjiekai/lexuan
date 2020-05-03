package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminCategoryDTO;
import com.zhuanbo.core.entity.Category;
import com.zhuanbo.service.vo.CategoryVO;

import java.util.List;

/**
 * <p>
 * 商品类目表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
public interface ICategoryService extends IService<Category> {

   /**
    * @Description(描述): 查询列表，模糊查询
    * @auther: Jack Lin
    * @param :[page, limit, id, name]
    * @return :java.lang.Object
    * @date: 2019/7/5 17:50
    */
    public Object queryList(AdminCategoryDTO dto);

    /**
     * 删除类目，类目下有商品不允许删除
     *
     * @param id
     * @return
     */
    public Object deleteCategory(Long id);

    /**
     *  新增类目
      * @param dto
     * @return
     */
    public Object creatCategory(AdminCategoryDTO dto);

    /**
     *  更新
     * @param dto
     * @return
     */
    public Object updateCategory(AdminCategoryDTO dto);

    /**
     * @Description(描述): 根据id批量查询
     * @auther: Jack Lin
     * @param :[ids]
     * @return :java.util.List<com.zhuanbo.core.entity.Category>
     * @date: 2019/7/5 10:52
     */
    public List<Category> selectBatchIds(List<Long> ids);

    /**
     * @Description(描述): 编辑
     * @auther: Jack Lin
     * @param :[id]
     * @return :java.lang.Object
     * @date: 2019/7/5 15:40
     */
    public Object detail(Long id);

    /**
     * @Description(描述):查询一条记录
     * @auther: Jack Lin
     * @param :[id]
     * @return :com.zhuanbo.core.entity.Category
     * @date: 2019/7/10 16:46
     */
    public CategoryVO selectOne(Long id);

}
