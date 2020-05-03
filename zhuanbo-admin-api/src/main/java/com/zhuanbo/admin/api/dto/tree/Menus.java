package com.zhuanbo.admin.api.dto.tree;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Menus {

    public Menus(Integer id, Integer pid, String name, String url, Integer type) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.url = url;
        this.type = type;
    }

    private Integer id;
    private String name;
    private String url;
    private Integer type;
    @JSONField(serialize = false)
    private Integer pid;
    private Boolean checked = false;
    private List<Menus> btns = new ArrayList<>();
    private List<Menus> menus = new ArrayList<>();
    private List<Menus> children = new ArrayList<>();
}
