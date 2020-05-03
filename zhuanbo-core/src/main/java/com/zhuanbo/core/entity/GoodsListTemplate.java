package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品列表模板
 */
@TableName("shop_goods_list_template")
@Data
public class GoodsListTemplate implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long index;
    /**
     * 商品内容,0:分类商品,1:指定商品
     */
    private Integer goodsType;
    /**
     * 指定的商品id
     */
    private Long goodsId;
    /**
     * 商品的排序，0：时间，1：销量
     */
    private Integer sortType;
    /**
     * 显示数量
     */
    private Long displayNum;
    /**
     * 显示风格,0:1行1个，1:1行2个，2:1行3个，3:1行4个
     */
    private Integer displayType;
    /**
     * 是否显示标题,0:否，1：是
     */
    private Integer isDisplayTitle;
    /**
     * 是否显示原价,0:否，1：是
     */
    private Integer isDisplayPrice;
    /**
     * 是否显示角标,0:否，1：是
     */
    private Integer isDisplayMark;
    /**
     * 是否显示销量,0:否，1：是
     */
    private Integer isDisplayNum;
    private Long templatePid;

    private LocalDateTime createTime;
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

}
