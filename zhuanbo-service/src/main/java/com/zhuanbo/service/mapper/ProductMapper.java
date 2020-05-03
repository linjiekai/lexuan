package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 商品货品表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 更新数量
     * @param productId 货品id
     * @param num 数量
     * @return > 0: 更新成功，<=0：更新失败
     */
    @Update("update shop_product set stock = stock - #{num} where id = #{productId} and stock >= #{num} and deleted = 0")
    int updateNumber(@Param("productId") Integer productId, @Param("num") Integer num);
}
