package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.ProductDTO;
import com.zhuanbo.core.entity.Product;

/**
 * <p>
 * 商品货品表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IProductService extends IService<Product> {

    /**
     * 更新数量
     * @param productId 货品id
     * @param num 数量
     * @return ture: 更新成功，false：更新失败
     */
    boolean updateNumber(Integer productId, Integer num);

    /**
     * 调用php系统,获取商品货品信息
     * @return
     */
    ProductDTO findProductDTOById(Integer productId);
}
