package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Data
@TableName("shop_admin_dealer")
public class AdminDealer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 管理员名称
     */
    private String username;

    /**
     * 管理员密码
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 最近一次登录IP地址
     */
    private String lastLoginIp;

    /**
     * 最近一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 头像图片
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Integer deleted;

    /**
     * 乐观锁字段
     */
    private Integer version;

    private Integer roleId;
}
