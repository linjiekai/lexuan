package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

//广告模板
@TableName("shop_ad_template")
@Data
public class AdTemplate implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 模板中排序索引
     */
    private Long index;
    /**
     * 显示风格，0:1行1个，1:1行2个，2:1行3个，3:1行4个，4:2左2右，5:1左2右，6:1上2下，7:1左3右
     */
    private Integer displayType;
    /**
     * 是否横向轮播，0：否，1：是
     */
    private Integer acrossCarousel;
    /**
     * 图片地址列表
     */
    private String[] banners;
    /**
     * 跳转类型, 0：H5链接，1:商品详情，2：商品分类页，3：自建装修页（下拉标题）
     */
    private Integer[] turnType;
    /**
     * 跳转链接列表
     */
    private String[] turnUrl;
    /**
     * 标题列表
     */
    private String[] titles;
    /**
     * 父模板id
     */
    private Long templatePid;

    private LocalDateTime createTime;
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
