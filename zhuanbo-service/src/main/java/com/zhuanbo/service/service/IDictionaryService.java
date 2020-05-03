package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.DictionaryDTO;
import com.zhuanbo.core.entity.Dictionary;

import java.util.List;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-07-01
 */
public interface IDictionaryService extends IService<Dictionary> {

    Dictionary findByCategoryAndName(String category, String name);

    /**
     * 返回String value
     * @param category
     * @param name
     * @return
     */
    String findForString(String category, String name);

    /**
     * 返回Long value
     * @param category
     * @param name
     * @return
     */
    Long findForLong(String category, String name);

    /**
     * 缓存公司账号
     * @return
     */
    Long findCompanyIdCache();

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    IPage<Dictionary> pageInfo(DictionaryDTO dto);

    /**
     * 字典数据编辑
     *
     * @param adminId
     * @param dto
     * @return
     */
    boolean edit(Integer adminId, DictionaryDTO dto);

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    IPage<Dictionary> pageWithdr(DictionaryDTO dto);

    /**
     * 按类型查询
     * @param category
     * @return
     */
    List<Dictionary> findByCategory(String category);

    /**
     * 按类型查询(缓存)
     * @param category
     * @return
     */
    List<Dictionary> findByCategoryCache(String category);

}
