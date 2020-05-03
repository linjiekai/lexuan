package com.zhuanbo.admin.api.dto.goods;

import com.zhuanbo.core.entity.GoodsAttribute;
import com.zhuanbo.core.entity.GoodsSpecification;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性规格DTO
 */
@Data
public class AttributeDTO {

    private GoodsAttribute goodsAttribute;
    private List<GoodsSpecification> goodsSpecifications = new ArrayList<>();
}
