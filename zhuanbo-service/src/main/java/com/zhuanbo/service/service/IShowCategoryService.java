package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminShowCategoryDTO;
import com.zhuanbo.core.entity.ShowCategory;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 展示类目表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
public interface IShowCategoryService extends IService<ShowCategory> {

    /**
     * 设置下级数据到缓存
     * @param id
     * @throws Exception
     */
	void setChildrenCache(Long id) throws Exception;
	
	/**
	 * 设置实体到缓存
	 * @param id
	 * @throws Exception
	 */
	void setShowCategoryCache(Long id) throws Exception;
	
	/**
     * 从缓存获取实体
     * @param id
     * @return
     * @throws Exception
     */
	ShowCategory getShowCategoryCache(Long id) throws Exception;
	
    /**
     * 获取用户下级数据
     * @param id
     * @throws Exception
     */
    List<ShowCategory> getChildren(Map<String, Object> params) throws Exception;
    
    /**
     * 获取用户下级数据，从缓存获取
     * @param id
     * @throws Exception
     */
    List<ShowCategory> getChildrenCache(Long id) throws Exception;
    
    /**
     * 获取用户所有下级数据
     * @param data
     * @param id
     * @throws Exception
     */
    void getChildrenAll(List<ShowCategory> data, Map<String, Object> params) throws Exception;
    
    /**
     * 获取所有下级数据，从缓存获取
     * @param data
     * @param id
     * @throws Exception
     */
    void getChildrenAllCache(List<ShowCategory> data, Long id) throws Exception;
    
    /**
     * 获取父节点(一级节点)
     * @param userId
     * @return
     */
    ShowCategory getParentRoot(Long id) throws Exception;
    
    /**
     * 从缓存获取父节点(一级节点)
     * @param userId
     * @return
     */
    ShowCategory getParentRootCache(Long id) throws Exception;
    
    /**
     * 删除
     * @param userId
     * @return
     */
    void deleteCache(Long id) throws Exception;
    
    /**
     * 刷新缓存
     * @param 
     * @return
     */
    void refreshCache(Long id) throws Exception;
    
    /**
     * 刷新缓存
     * @param 
     * @return
     */
    void refreshAllCache() throws Exception;
    
    /**
     * 模糊查询
     * @param dto
     * @return
     */
    Object queryList(AdminShowCategoryDTO dto) throws Exception;

    /**
     *  删除
     * @param id
     * @return
     */
    Object deleteShowCategory(Long id) throws Exception;

    /**
     * 创建
     * @param dto
     * @return
     * @throws Exception 
     */
    Object creatShowCategory(AdminShowCategoryDTO dto) throws Exception;

    /**
     * 更新
     * @param dto
     * @return
     * @throws Exception 
     */
    Object updateShowCategory(AdminShowCategoryDTO dto) throws Exception;

    /**
     * @Description(描述): 编辑
     * @auther: Jack Lin
     * @param :[id]
     * @return :java.lang.Object
     * @date: 2019/7/5 15:40
     */
    Object detail(Long id) throws Exception;
    /**
     * 刷新首页 展示一级分类缓存
     * @param
     * @return
     */
    void refreshIndexShowCategoryCache() throws Exception;
}
