package com.zhuanbo.service.vo;


import com.zhuanbo.core.entity.Goods;
import lombok.Data;

@Data
public class GoodsDisplayVO extends Goods {

    /**
     * 类型[0:普通商品, 1:爆款]
     */
    private Integer type;

}
