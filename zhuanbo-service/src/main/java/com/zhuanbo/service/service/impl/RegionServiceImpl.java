package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Region;
import com.zhuanbo.service.mapper.RegionMapper;
import com.zhuanbo.service.service.IRegionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 行政区域表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements IRegionService {

    @Override
    public Object children(Integer pid, Integer level) {

        List<Region> list = new ArrayList<>();

        Optional.ofNullable(pid).ifPresent(p -> {
            Optional.ofNullable(level).ifPresent(l ->{
                listChildren(list, pid, level);
            });
        });
        return list;
    }

    @Override
    public void deleteLevelByLevel(Integer id) {
        removeById(id);
        QueryWrapper<Region> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", id);
        list(queryWrapper).forEach(x -> deleteLevelByLevel(x.getId()));
    }

    private void listChildren(List<Region> list, Integer pid, Integer level) {

        if (level.equals(0)) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("pid", pid);
            list.addAll(list(queryWrapper));
        } else {
            getChildren(list, pid);
        }
    }

    private void getChildren(List<Region> list, Integer pid) {

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pid", pid);
        List<Region> l = list(queryWrapper);
        if (!l.isEmpty()) {
            list.addAll(l);
            l.forEach(x -> {
                getChildren(list, x.getId());
            });
        }
    }
}
