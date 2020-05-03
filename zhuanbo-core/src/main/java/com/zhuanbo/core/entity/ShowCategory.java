package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 展示类目表
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
@TableName("shop_show_category")
@Data
public class ShowCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 父类目ID
     */
    private Long pid;

    /**
     * 类目名称
     */
    private String name;

    /**
     * 层次 1：一级 2：二级 3：三级
     */
    private Integer level;

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
     * 是否展示 0：不展示 1：展示
     */
    private Integer enable;

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
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<ShowCategory> childrenList;

    /**
     * 三级分类关联表
     */
    @TableField(exist = false)
    private List<CategoryRelation> categoryRelationList;

    /**
     * 首页的分类列表主图
     */
    private String showIndexUrl;

}
