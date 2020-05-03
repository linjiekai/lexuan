package com.zhuanbo.core.dto;

import com.zhuanbo.core.entity.GoodsAttribute;
import com.zhuanbo.core.entity.GoodsSpecification;
import lombok.Data;

import java.util.List;


@Data
public class AdminGoodsAttributeDTO extends GoodsAttribute {
    private List<GoodsSpecification> specifications;
}
