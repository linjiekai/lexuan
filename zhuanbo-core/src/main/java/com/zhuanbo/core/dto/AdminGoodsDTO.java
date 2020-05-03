package com.zhuanbo.core.dto;

import com.zhuanbo.core.entity.Goods;
import lombok.Data;

import java.util.List;


@Data
public class AdminGoodsDTO extends AdminBaseRequestDTO{
    private Goods goods;
    private List<AdminGoodsAttributeDTO> attributes;
}
