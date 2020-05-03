package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author rome
 * @since 2019-05-10
 */
@Data
@TableName("shop_role_permission")
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    public RolePermission(){// 留着

    }
    public RolePermission(Integer roleId, Integer permissionId){
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer roleId;

    private Integer permissionId;
}
