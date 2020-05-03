package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.role.RolePermissionDTO;
import com.zhuanbo.admin.api.dto.tree.ElementTree;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.core.entity.Permission;
import com.zhuanbo.core.entity.Role;
import com.zhuanbo.core.entity.RolePermission;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IPermissionService;
import com.zhuanbo.service.service.IRolePermissionService;
import com.zhuanbo.service.service.IRoleService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/role")
public class RoleController {

    @Autowired
    private IRoleService iRoleService;
    @Autowired
    private IRolePermissionService iRolePermissionService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private IPermissionService iPermissionService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_ROLE_UPDATE = "lock_role_update_";
    private final static String LOCK_ADMIN_UPDATE = "lock_admin_update_";

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       Role role) {


        Page<Role> pageCond = new Page<>(page, limit);
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(sort);
        queryWrapper.eq("deleted", ConstantsEnum.ROLE_DELETED_0.integerValue());
        if (role != null) {
            if (role.getId() != null) {
                queryWrapper.eq("id", role.getId());
            }
        }

        IPage<Role> iPage = iRoleService.page(pageCond, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", limit.equals(-1) ? iRoleService.count(queryWrapper) : iPage.getTotal());
        data.put("items", iPage.getRecords());
        return ResponseUtil.ok(data);
    }

    @Transactional
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody RolePermissionDTO rolePermissionDTO) {
        LogOperateUtil.log("角色管理", "创建", null, adminId.longValue(), 0);
        if (StringUtils.isBlank(rolePermissionDTO.getName())) {
            return ResponseUtil.fail("11111", "缺少参数:name");
        }
        Role role = new Role();
        role.setName(rolePermissionDTO.getName());
        role.setAddTime(LocalDateTime.now());
        role.setUpdateTime(role.getAddTime());
        iRoleService.save(role);

        List<Integer> permissionIds = rolePermissionDTO.getPermissionIds();
        if (permissionIds != null && permissionIds.size() > 0) {
            List<RolePermission> list =
                    permissionIds.stream().map(x -> new RolePermission(role.getId(), x)).collect(Collectors.toList());
            iRolePermissionService.saveBatch(list);
        }

        Role role1 = new Role();
        role1.setId(role.getId());
        return list(adminId, 1, 1, "add_time", role1);
    }

    @Transactional
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody RolePermissionDTO rolePermissionDTO) {
        LogOperateUtil.log("角色管理", "修改", String.valueOf(rolePermissionDTO.getRoleId()), adminId.longValue(), 0);
        if (rolePermissionDTO.getRoleId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:roleId");
        }
        if (StringUtils.isBlank(rolePermissionDTO.getName())) {
            return ResponseUtil.fail("11111", "缺少参数:name");
        }

        Role oldRole = iRoleService.getById(rolePermissionDTO.getRoleId());
        oldRole.setName(rolePermissionDTO.getName());
        oldRole.setUpdateTime(LocalDateTime.now());
        String key = LOCK_ROLE_UPDATE + oldRole.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 60);
        if (!b) {
            return ResponseUtil.result(30014);
        }
        try {
            iRoleService.updateById(oldRole);
            // 删除之前的
            iRolePermissionService.remove(new UpdateWrapper<RolePermission>().eq("role_id", oldRole.getId()));
            // 保存现在的
            if (rolePermissionDTO.getPermissionIds() != null && rolePermissionDTO.getPermissionIds().size() > 0) {
                List<RolePermission> list =
                        rolePermissionDTO.getPermissionIds().stream().map(x -> new RolePermission(rolePermissionDTO.getRoleId(), x)).collect(Collectors.toList());
                iRolePermissionService.saveBatch(list);
            }

            Role role1 = new Role();
            role1.setId(oldRole.getId());
            return list(adminId, 1, 1, "add_time", role1);
        } catch (Exception e) {
            throw e;
        } finally {
            if(b){
                redissonLocker.unlock(key);
            }

        }

    }


    @Transactional
    @PostMapping("/del")
    public Object del(@LoginAdmin Integer adminId, @RequestBody Role role) {
        LogOperateUtil.log("角色管理", "删除", String.valueOf(role.getId()), adminId.longValue(), 0);
        if (role.getId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:id");
        }
        String key = LOCK_ROLE_UPDATE + role.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 60);
        if (!b) {
            return ResponseUtil.result(30014);
        }
        try{
            iRoleService.update(new Role(), new UpdateWrapper<Role>()
                    .set("deleted", ConstantsEnum.ROLE_DELETED_1.integerValue())
                    .set("update_time", LocalDateTime.now())
                    .eq("id", role.getId()));

            return ResponseUtil.ok();
        } catch (Exception e) {
            throw e;
        } finally {
            if(b){
                redissonLocker.unlock(key);
            }
        }


    }

    @Transactional
    @PostMapping("/bindPer")
    public Object bindPer(@LoginAdmin Integer adminId, @RequestBody RolePermissionDTO rolePermissionDTO) {
        LogOperateUtil.log("角色管理", "角色权限绑定", String.valueOf(rolePermissionDTO.getRoleId()), adminId.longValue(), 0);

        if (rolePermissionDTO.getRoleId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:roleId");
        }
        if (rolePermissionDTO.getPermissionIds() == null) {
            return ResponseUtil.fail("11111", "缺少参数:permissionIds");
        }
        // 删除之前的
        iRolePermissionService.remove(new UpdateWrapper<RolePermission>().eq("role_id", rolePermissionDTO.getRoleId()));
        // 保存现在的
        if (rolePermissionDTO.getPermissionIds().size() > 0) {
            List<RolePermission> list =
                    rolePermissionDTO.getPermissionIds().stream().map(x -> new RolePermission(rolePermissionDTO.getRoleId(), x)).collect(Collectors.toList());
            iRolePermissionService.saveBatch(list);
        }
        return ResponseUtil.ok();
    }

    @PostMapping("/bindAdmin")
    public Object bindAdmin(@LoginAdmin Integer adminId, @RequestBody RolePermissionDTO rolePermissionDTO) {
        LogOperateUtil.log("角色管理", "角色管理员绑定", String.valueOf(rolePermissionDTO.getAdminId()), adminId.longValue(), 0);
        if (rolePermissionDTO.getRoleId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:roleId");
        }
        if (rolePermissionDTO.getAdminId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:adminId");
        }
        String key = LOCK_ADMIN_UPDATE + rolePermissionDTO.getAdminId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 30);
        if (!b) {
            return ResponseUtil.result(30014);
        }
        try{
            iAdminService.update(new Admin(), new UpdateWrapper<Admin>().set("role_id", rolePermissionDTO.getRoleId()).eq("id", rolePermissionDTO.getAdminId()));
            return ResponseUtil.ok();
        }catch(Exception e){
            throw  e;
        }finally {
            if(b){
                redissonLocker.unlock(key);
            }
        }

    }

    @GetMapping("/perList")
    public Object perList(@LoginAdmin Integer adminId, RolePermissionDTO rolePermissionDTO) {

        if (rolePermissionDTO.getRoleId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:roleId");
        }

        List<ElementTree> finalList = new ArrayList<>();

        List<RolePermission> rolePermissionList = iRolePermissionService.list(new QueryWrapper<RolePermission>().eq("role_id", rolePermissionDTO.getRoleId()));
        if (!rolePermissionList.isEmpty()) {

            List<Integer> permissionIds = rolePermissionList.stream().map(x -> x.getPermissionId()).collect(Collectors.toList());
            List<Permission> permissionList = iPermissionService.list(new QueryWrapper<Permission>()
                    .in("id", permissionIds).eq("deleted", ConstantsEnum.DELETED_0));

            if (!permissionList.isEmpty()) {

                Map<Integer, ElementTree> mapTmp = new HashMap<>();
                for (Permission current : permissionList) {
                    mapTmp.put(current.getId(), new ElementTree(current.getId(), current.getPid(), current.getName(),
                            current.getLevel(), current.getIcon(), current.getUrl(), current.getType()));
                }
                mapTmp.forEach((k, v) -> {
                    if (v.getPid() == null || v.getPid().equals(0)) {
                        finalList.add(v);
                    } else {
                        if (mapTmp.get(v.getPid()) != null) {
                            mapTmp.get(v.getPid()).getChildren().add(v);
                        } else {
                            finalList.add(v);
                        }
                    }
                });
            }
        }
        return ResponseUtil.ok(finalList);
    }

    /**
     * 显示所有的权限，并通过check区分哪些是选中的
     *
     * @param adminId
     * @param role
     * @return
     */
    @GetMapping("/detail")
    public Object detail(@LoginAdmin Integer adminId, Role role) {

        if (role.getId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:id");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("role", iRoleService.getById(role.getId()));// 角色信息

        // 所有权限与被选中的
        List<ElementTree> finalList = new ArrayList<>();// 树
        List<Permission> permissionList = iPermissionService.list(new QueryWrapper<Permission>().eq("deleted", ConstantsEnum.DELETED_0));// 所有的
        List<RolePermission> rolePermissionList = iRolePermissionService.list(new QueryWrapper<RolePermission>().eq("role_id", role.getId()));// 当前用户的
        List<Integer> currentRolePerList = new ArrayList<>();// 当前用户的权限
        if (!rolePermissionList.isEmpty()) {
            for (RolePermission rolePermission : rolePermissionList) {
                currentRolePerList.add(rolePermission.getPermissionId());
            }
        }

        if (!permissionList.isEmpty()) {

            Map<Integer, ElementTree> mapTmp = new TreeMap<>();
            for (Permission current : permissionList) {
                mapTmp.put(current.getId(), new ElementTree(current.getId(), current.getPid(), current.getName(),
                        current.getLevel(), current.getIcon(), current.getUrl(), current.getType()));
            }
            mapTmp.forEach((k, v) -> {
                if (currentRolePerList.contains(v.getId())) {
                    v.setChecked(true);
                }
                if (v.getPid() == null || v.getPid().equals(0)) {
                    finalList.add(v);
                } else {
                    if (mapTmp.get(v.getPid()) != null) {
                        mapTmp.get(v.getPid()).getChildren().add(v);
                    } else {
                        finalList.add(v);
                    }
                }
            });
        }

        map.put("perList", finalList);
        map.put("selected", currentRolePerList);
        map.put("selectedIdLabel", selectedIdLable(role.getId()));
        return ResponseUtil.ok(map);
    }

    /**
     * 只显示角色有的权限
     *
     * @param adminId
     * @param role
     * @return
     */
    @GetMapping("/detail/check")
    public Object detailCheck(@LoginAdmin Integer adminId, Role role) {

        if (role.getId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:id");
        }

        // 所有权限与被选中的
        List<ElementTree> finalList = new ArrayList<>();// 树

        List<RolePermission> rolePermissionList = iRolePermissionService.list(new QueryWrapper<RolePermission>().eq("role_id", role.getId()));// 当前用户的角色权限关系
        if (!rolePermissionList.isEmpty()) {
            List<Integer> rolePermissionIds = rolePermissionList.stream().map(x -> x.getPermissionId()).collect(Collectors.toList());
            List<Permission> permissionList = iPermissionService.list(new QueryWrapper<Permission>().in("id", rolePermissionIds)
                    .eq("deleted", ConstantsEnum.DELETED_0.integerValue()));// 角色对应的权限

            if (!permissionList.isEmpty()) {

                Map<Integer, ElementTree> mapTmp = new TreeMap<>();
                for (Permission current : permissionList) {
                    mapTmp.put(current.getId(), new ElementTree(current.getId(), current.getPid(), current.getName(),
                            current.getLevel(), current.getIcon(), current.getUrl(), current.getType()));
                }
                mapTmp.forEach((k, v) -> {
                    System.out.println(v.getId() + ":" + v.getPid() + "xxxxxxxxxx");
                    if (v.getPid() == null || v.getPid().equals(0)) {
                        finalList.add(v);
                    } else {
                        if (mapTmp.get(v.getPid()) != null) {
                            mapTmp.get(v.getPid()).getChildren().add(v);
                        } else {
                            finalList.add(v);
                        }
                    }
                });
            }
        }
        return ResponseUtil.ok(finalList);
    }

    /**
     * 根据roleId获取选中的叶子节点
     *
     * @param roleId
     * @return
     */
    private Object selectedIdLable(Integer roleId) {

        // 所有权限与被选中的
        List<Map<String, Object>> finalList = new ArrayList<>();// 树

        List<RolePermission> rolePermissionList = iRolePermissionService.list(new QueryWrapper<RolePermission>().eq("role_id", roleId));// 当前用户的角色权限关系
        if (!rolePermissionList.isEmpty()) {
            List<Integer> rolePermissionIds = rolePermissionList.stream().map(x -> x.getPermissionId()).collect(Collectors.toList());
            List<Permission> permissionList = iPermissionService.list(new QueryWrapper<Permission>().in("id", rolePermissionIds)
                    .eq("deleted", ConstantsEnum.DELETED_0.integerValue()));// 角色对应的权限

            if (!permissionList.isEmpty()) {

                Map<Integer, Integer> mapTmp = new HashMap<>();
                for (Permission current : permissionList) {
                    mapTmp.put(current.getPid(), 0);
                }

                for (Permission permission : permissionList) {
                    if (mapTmp.get(permission.getId()) == null) {
                        finalList.add(MapUtil.of("id", permission.getId(), "label", permission.getName()));
                    }
                }
            }
        }
        return finalList;
    }
}
