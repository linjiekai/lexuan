package com.zhuanbo.admin.api.dto.role;

import com.zhuanbo.core.entity.Permission;
import com.zhuanbo.core.entity.Role;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionDTO {
    private Role role;
    private List<Permission> permissionList;
    private Integer roleId;
    private List<Integer> permissionIds;
    private Integer adminId;
    private String name;
}
