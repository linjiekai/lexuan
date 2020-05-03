package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.MobileGoodsDisplayDTO;
import com.zhuanbo.core.entity.GoodsDisplay;


public interface IGoodsDisplayService extends IService<GoodsDisplay> {

    Object listByUserId(Long userId);

    Object addPrimaryGoods(MobileGoodsDisplayDTO dto);
}
