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
 * 商品类目表
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
@TableName("shop_category")
@Data
public class Category implements Serializable {

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
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
