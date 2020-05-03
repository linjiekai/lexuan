package com.zhuanbo.admin.api.dto.req;


import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.GoodsAttribute;
import com.zhuanbo.core.entity.GoodsSpecification;
import com.zhuanbo.core.entity.Product;
import lombok.Data;

@Data
public class GoodsAllinoneDTO {
    Goods goods;
    GoodsSpecification[] specifications;
    GoodsAttribute[] attributes;
    Product[] products;

}
