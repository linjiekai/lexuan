package com.zhuanbo.admin.api.dto.tree;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ElementTree {

    public ElementTree(Integer id, Integer pid, String label, Integer level, String icon, String url, Integer type) {
        this.id = id;
        this.pid = pid;
        this.label = label;
        this.level = level;
        this.icon = icon;
        this.url = url;
        this.type = type;
    }

    private Integer id;
    private String label;
    @JSONField(serialize = false)
    private Integer pid;
    private Boolean checked = false;
    private Integer level;
    private String icon;
    private String url;
    private Integer type;
    private List<ElementTree> children = new ArrayList<>();
}
