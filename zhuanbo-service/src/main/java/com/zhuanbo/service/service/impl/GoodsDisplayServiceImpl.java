package com.zhuanbo.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.dto.MobileGoodsDisplayDTO;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.GoodsDisplay;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.mapper.GoodsDisplayMapper;
import com.zhuanbo.service.service.IGoodsDisplayService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.vo.GoodsDisplayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class GoodsDisplayServiceImpl extends ServiceImpl<GoodsDisplayMapper, GoodsDisplay> implements IGoodsDisplayService {

    @Autowired
    IGoodsService iGoodsService;

    @Override
    public Object listByUserId(Long userId) {

        List<GoodsDisplayVO> list = CollectionUtil.newArrayList();

        List<GoodsDisplay> goodsDisplays = baseMapper.selectList(new QueryWrapper<GoodsDisplay>().eq("user_id", userId));
        List<Long> ids = goodsDisplays.stream().map(s -> s.getGoodsId()).collect(Collectors.toList());
       if(CollectionUtil.isNotEmpty(ids)){
           List<Goods> goods = iGoodsService.selectBatchIds(ids);
           goods.stream().forEach((s) -> {
               GoodsDisplayVO vo = new GoodsDisplayVO();
               BeanUtil.copyProperties(s, vo);
               //首推商品设置回去
               goodsDisplays.forEach(n->{
                   if(s.getId().intValue()==n.getId().intValue()){
                       vo.setType(n.getType());
                   }
               });
               list.add(vo);
           });
       }
        return ResponseUtil.ok(list);
    }

    @Override
    public Object addPrimaryGoods(MobileGoodsDisplayDTO dto) {
        Goods goods = iGoodsService.getById(dto.getGoodsId());
        Optional.ofNullable(goods).orElseThrow(() -> new ShopException(30001));
        if (1 != goods.getStatus().intValue()) {
            throw new ShopException(30006);
        }
        baseMapper.update(new GoodsDisplay(), new UpdateWrapper<GoodsDisplay>().eq("type", 1).set("type", 0));

        baseMapper.update(new GoodsDisplay(), new UpdateWrapper<GoodsDisplay>().eq("live_group_id", dto.getLiveGroupId())
                .eq("user_id", dto.getUserId()).eq("goods_id", dto.getGoodsId()).set("type", 1));

        return ResponseUtil.ok();
    }
}
