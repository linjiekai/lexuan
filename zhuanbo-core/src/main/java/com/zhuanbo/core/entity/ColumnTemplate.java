package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导航模板
 */
@TableName("shop_column_template")
@Data
public class ColumnTemplate implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long index;
    /**
     * 商品个数
     */
    private Long goodsNum;
    /**
     * 背景图
     */
    private String[] 图片地址列表;
    /**
     * 名称列表
     */
    private String[] names;
    /**
     * 商品id列表
     */
    private Long[] goodsIds;
    /**
     * 背景图
     */
    private String bgBanner;
    /**
     * 父模板id
     */
    private Long templatePid;

    private LocalDateTime createTime;
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
