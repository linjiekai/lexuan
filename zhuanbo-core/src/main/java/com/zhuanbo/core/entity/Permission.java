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
 * 
 * </p>
 *
 * @author rome
 * @since 2019-05-10
 */
@Data
@TableName("shop_permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 父id
     */
    private Integer pid;

    /**
     * 名称
     */
    private String name;

    /**
     * 路由或按钮标识符
     */
    private String url;

    /**
     * 类型。0：菜单或路由、1：按钮
     */
    private Integer type;

    /**
     * 删除。0：否，1：是
     */
    private Integer deleted;

    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    private Integer level;

    private String icon;
}
