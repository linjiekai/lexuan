package com.zhuanbo.core.dto;

import com.zhuanbo.core.constants.CategoryLevel;
import lombok.Data;

@Data
public class AdminShowCategoryDTO extends AdminBaseRequestDTO {
    public Long id;
    public String name;
    //层级
    private Integer level = CategoryLevel.LEVEL1.getId();
    private Long pid;
    /**
     * 排序
     */
    private Integer indexs;
    /**
     * 分类页图标
     */
    private String iconUrl;
    /**
     * 展示分类页banner
     */
    private String showUrl;
    /**
     * 产品展示页banner
     */
    private String productUrl;

    /**
     * 状态 1:上线 2：下线
     */
    private Integer status;

    /**
     * 操作人ID
     */
    private Integer adminId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 逻辑删除
     */
    private Integer deleted;

    /**
     * 类目id
     */
    private Long[] cateIds;
    /**
     * 商品id
     */
    private Long[] goodsIds;
    /**
     * 是否展示 0：不展示 1：展示
     */
    private Integer enable;
    /**
     * 首页的分类列表banner
     */
    private String showIndexUrl;

}
