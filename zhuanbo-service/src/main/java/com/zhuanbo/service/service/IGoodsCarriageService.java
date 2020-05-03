package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.GoodsCarriage;

import java.math.BigDecimal;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author rome
 * @since 2019-03-12
 */
public interface IGoodsCarriageService extends IService<GoodsCarriage> {
    /**
     * 拿一条，根据type处理(目前type没什么用)
     * @param type
     * @return
     */
    BigDecimal findOne(Integer type);

    /**
     * 计算运费，运费=运费模板价格✖购买数量/3
     * @param num 购买数量
     * @return
     */
    BigDecimal calculateShipping(Integer num);
}
