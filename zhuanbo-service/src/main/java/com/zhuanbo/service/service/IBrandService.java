package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Brand;

import java.util.List;

/**
 * <p>
 * 品牌商表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-07-26
 */
public interface IBrandService extends IService<Brand> {

    /**
     * @Description(描述): mobile查询列表
     * @auther: Jack Lin
     * @param :[]
     * @return :java.lang.Object
     * @date: 2019/7/26 14:28
     */
    Object listByMobile(Integer page, Integer limit) throws Exception;

    Object detailByMobile(Long id) throws Exception;
    /**
     * 获取所有全部品牌id
     * @return
     */
    List<Long> findBrandId();

    /**
     * 添加品牌时把数据设置到redis
     * @param brand
     */
    void createBrandInfoIntoRedis(Brand brand);


    /**
     * 删除品牌时删除redis中的数据
     * @param brand
     */
    void deleteBrandInfoRedis(Brand brand);

    /**
     * 更新品牌缓存
     */
    void updateRedisBrandInfo();
    
    /**
     * 更新品牌缓存
     */
    void refreshRedisBrandInfo();

}
