package com.zhuanbo.shop.api.dto.resp;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 区域树
 */
@Data
public class RegionTreeDTO {

    public RegionTreeDTO(Integer id, String name, Integer pId){
        this.id = id;
        this.name = name;
        this.pId = pId;
    }

    private Integer id;
    private String name;
    @JSONField(serialize = false)
    private Integer pId;
    private List<RegionTreeDTO> children = new ArrayList<>();
}
