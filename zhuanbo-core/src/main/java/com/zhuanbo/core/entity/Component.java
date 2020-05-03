package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("shop_component")
@Data
public class Component implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 组件名称
     */
    private String name;
    /**
     * 组件图标
     */
    private String banner;
    /**
     * 组件关联模板类型 0:导航膜版，1:广告模板，2:商品列表模板，3:秒杀模板，4:辅助线，5:空白格
     */
    private Integer templateType;
    private Long index;

    private LocalDateTime createTime;
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
