package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.UserGoods;


public interface IUserGoodsService extends IService<UserGoods> {

    public Object listByUserId(Long userId);

}
