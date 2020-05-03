package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Region;

/**
 * <p>
 * 行政区域表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IRegionService extends IService<Region> {

    /**
     * 根据父ID获取子、孙数据
     * @param pid 父ID
     * @param level 0:只获取子级数组，1：获取子、孙等全部后代数据
     * @return
     */
    Object children(Integer pid, Integer level);

    /**
     * 删除，同时删除其下面的子、孙等所有节点数据
     * @param id
     */
    void deleteLevelByLevel(Integer id);
}
