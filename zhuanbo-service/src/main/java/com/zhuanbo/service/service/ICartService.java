package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.MobileCartGoodsSysDTO;
import com.zhuanbo.core.entity.Cart;

import java.util.List;

/**
 * <p>
 * 购物车商品表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface ICartService extends IService<Cart> {

    Object addCart(Long userId, Cart cart, boolean checked, Integer buyType) throws Exception;

    Integer calculateGoodsCount(Long userId);
    
    public Integer maxTraceType(List<Cart> checkedGoodsList);

    /**
     * 商品修改购物车数据同步
     * @param mobileCartGoodsSysDTO
     */
    void goodsSys(MobileCartGoodsSysDTO mobileCartGoodsSysDTO);

}
