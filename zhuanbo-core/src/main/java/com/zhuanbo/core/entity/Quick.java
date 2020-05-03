package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 快捷入口表
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
@TableName("shop_quick")
@Data
public class Quick implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类目名称
     */
    private String name;

    /**
     * 层次 1：一级 3：三级 5：H5
     */
    private Integer level;

    /**
     * 序号
     */
    private Integer indexs;

    /**
     * 关联show_category表ID
     */
    private Long categoryId;

    /**
     * 跳转链接
     */
    private String redirectUrl;

    /**
     * 快捷图标
     */
    private String iconUrl;

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
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;


}
