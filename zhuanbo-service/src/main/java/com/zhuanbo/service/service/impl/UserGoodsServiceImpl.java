package com.zhuanbo.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.UserGoods;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.mapper.UserGoodsMapper;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IUserGoodsService;
import com.zhuanbo.service.vo.UserGoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserGoodsServiceImpl extends ServiceImpl<UserGoodsMapper, UserGoods> implements IUserGoodsService {

    @Autowired
    IGoodsService iGoodsService;

    @Override
    public Object listByUserId(Long userId) {
        List<UserGoodsVO> userGoodsVOS = CollectionUtil.newArrayList();
        List<UserGoods> userGoods = this.baseMapper.selectList(new QueryWrapper<UserGoods>().eq("user_id", userId));
        List<Long> ids = userGoods.stream().map(s -> s.getGoodsId()).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(ids)){
            List<Goods> goods = iGoodsService.selectBatchIds(ids);
            goods.forEach((s) -> {
                UserGoodsVO vo = new UserGoodsVO();
                BeanUtil.copyProperties(s,vo);
                userGoodsVOS.add(vo);
            });
        }
        return ResponseUtil.ok(userGoodsVOS);
    }
}
