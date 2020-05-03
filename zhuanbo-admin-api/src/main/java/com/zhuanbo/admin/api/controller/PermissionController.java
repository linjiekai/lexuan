package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.tree.ElementTree;
import com.zhuanbo.admin.api.dto.tree.MenuSelect;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Permission;
import com.zhuanbo.service.service.IPermissionService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/permission")
public class PermissionController {

    @Autowired
    private IPermissionService iPermissionService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_PERMISSION_UPDATE = "lock_permission_update_";

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit,
                       @RequestParam(defaultValue = "add_time") String sort,
                       Permission permission) {


        Page<Permission> pageCond = new Page<>(page, limit);
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(sort);
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        if (permission != null) {
            if (permission.getId() != null) {
                queryWrapper.eq("id", permission.getId());
            }
        }

        IPage<Permission> iPage = iPermissionService.page(pageCond, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", limit.equals(-1) ? iPermissionService.count(queryWrapper) : iPage.getTotal());
        data.put("items", iPage.getRecords());
        return ResponseUtil.ok(data);
    }

    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody Permission permission) {
        LogOperateUtil.log("权限管理", "添加", null ,adminId.longValue(), 0);
        if (StringUtils.isBlank(permission.getName())) {
            return ResponseUtil.fail("11111", "缺少参数:name");
        }
        if (StringUtils.isBlank(permission.getUrl())) {
            return ResponseUtil.fail("11111", "缺少参数:url");
        }
        if (permission.getType() == null) {
            return ResponseUtil.fail("11111", "缺少参数:type");
        }
        permission.setAddTime(LocalDateTime.now());
        permission.setUpdateTime(permission.getAddTime());
        permission.setDeleted(ConstantsEnum.DELETED_0.integerValue());

        if (permission.getPid() != null && !permission.getPid().equals(0)) {
            Permission permissionParent = iPermissionService.getById(permission.getPid());
            if (permissionParent.getLevel() != null) {
                permission.setLevel(permissionParent.getLevel() + 1);
            }
        }

        iPermissionService.save(permission);

        Permission permission1 = new Permission();
        permission1.setId(permission.getId());
        return list(adminId,1,1, "add_time", permission1);
    }

    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody Permission permission) {
        LogOperateUtil.log("权限管理", "修改", String.valueOf(permission.getId()) ,adminId.longValue(), 0);
        if (StringUtils.isBlank(permission.getName())) {
            return ResponseUtil.fail("11111", "缺少参数:name");
        }
        if (StringUtils.isBlank(permission.getUrl())) {
            return ResponseUtil.fail("11111", "缺少参数:url");
        }
        if (permission.getType() == null) {
            return ResponseUtil.fail("11111", "缺少参数:type");
        }

        Permission oldPermission = iPermissionService.getById(permission.getId());
        Optional.ofNullable(permission.getName()).ifPresent(x -> oldPermission.setName(permission.getName()));
        Optional.ofNullable(permission.getPid()).ifPresent(x -> oldPermission.setPid(permission.getPid()));
        Optional.ofNullable(permission.getUrl()).ifPresent(x -> oldPermission.setUrl(permission.getUrl()));
        Optional.ofNullable(permission.getType()).ifPresent(x -> oldPermission.setType(permission.getType()));

        oldPermission.setUpdateTime(LocalDateTime.now());
        oldPermission.setIcon(permission.getIcon());
        if (permission.getPid() != null && !permission.getPid().equals(0)) {
            Permission permissionParent = iPermissionService.getById(permission.getPid());
            if (permissionParent.getLevel() != null) {
                oldPermission.setLevel(permissionParent.getLevel() + 1);
            }
        }
        String lockKey = LOCK_PERMISSION_UPDATE + permission.getId();
        boolean lock = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 5, 30);
        if (!lock) {
            return ResponseUtil.result(30014);
        }
        iPermissionService.updateById(oldPermission);
        if(lock){
            redissonLocker.unlock(lockKey);
        }
        Permission permission1 = new Permission();
        permission1.setId(oldPermission.getId());
        return list(adminId,1,1, "add_time", permission1);
    }


    @PostMapping("/del")
    public Object del(@LoginAdmin Integer adminId, @RequestBody Permission permission) {
        LogOperateUtil.log("权限管理", "删除", String.valueOf(permission.getId()) ,adminId.longValue(), 0);
        if (permission.getId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:id");
        }

        ArrayList<Integer> ids = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(permission.getId());

        while (!queue.isEmpty()) {
            Integer id = queue.poll();
            ids.add(id);
            List<Permission> sonList = iPermissionService.list(new QueryWrapper<Permission>().eq("pid", id));
            if (!sonList.isEmpty()) {
                sonList.forEach(x -> queue.offer(x.getId()));
            }
        }

        String lockKey = LOCK_PERMISSION_UPDATE + permission.getId();
        boolean lock = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 5, 30);
        if (!lock) {
            return ResponseUtil.result(30014);
        }
        iPermissionService.update(new Permission(), new UpdateWrapper<Permission>()
                .set("deleted", ConstantsEnum.ROLE_DELETED_1.integerValue())
                .set("update_time", LocalDateTime.now())
                .in("id", ids));
        if(lock){
            redissonLocker.unlock(lockKey);
        }


        return ResponseUtil.ok();
    }

    @GetMapping("/tree")
    public Object tree(@LoginAdmin Integer adminId) {

        List<Permission> list = iPermissionService.list(new QueryWrapper<Permission>().eq("deleted", ConstantsEnum.DELETED_0.integerValue()));

        List<ElementTree> finalList = new ArrayList<>();

        if (!list.isEmpty()) {

            Map<Integer, ElementTree> mapTmp = new HashMap<>();
            for (Permission current : list) {
                mapTmp.put(current.getId(), new ElementTree(current.getId(), current.getPid(), current.getName(),
                        current.getLevel(), current.getIcon(), current.getUrl(), current.getType()));
            }
            mapTmp.forEach((k, v) -> {
                if(v.getPid() == null || v.getPid().equals(0)) {
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
        return ResponseUtil.ok(finalList);
    }

    @GetMapping("/detail")
    public Object detail(@LoginAdmin Integer adminId, Permission permission) {

        if (permission.getId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:id");
        }
        return ResponseUtil.ok(iPermissionService.getById(permission.getId()));
    }

    @GetMapping("/selectList")
    public Object selectList(@LoginAdmin Integer adminId) {

        List<Permission> list = iPermissionService.list(new QueryWrapper<Permission>().eq("deleted", ConstantsEnum.DELETED_0.integerValue()));

        List<ElementTree> finalList = new ArrayList<>();

        if (!list.isEmpty()) {

            Map<Integer, ElementTree> mapTmp = new HashMap<>();
            for (Permission current : list) {
                mapTmp.put(current.getId(), new ElementTree(current.getId(), current.getPid(), current.getName(),
                        current.getLevel(), current.getIcon(), current.getUrl(), current.getType()));
            }
            mapTmp.forEach((k, v) -> {
                if(v.getPid() == null || v.getPid().equals(0)) {
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
        // 转成select格式
        List<MenuSelect> resultList = new ArrayList<>();
        if (finalList.size() > 0) {
            return ResponseUtil.ok(list(finalList));
        }
        return ResponseUtil.ok(resultList);
    }


    public List<MenuSelect> list(List<ElementTree> finalList) {

        List<MenuSelect> resultList = new ArrayList<>();
        Stack<ElementTree> nodeStack = new Stack<>();
        ElementTree node;
        for (ElementTree elementTree : finalList) {
            nodeStack.add(elementTree);
        }
        MenuSelect  menuSelect;
        StringBuffer stringBuffer;
        while (!nodeStack.isEmpty()) {

            node = nodeStack.pop();
            menuSelect = new MenuSelect();
            menuSelect.setId(node.getId());
            //menuSelect.setIcon(node.get);
            stringBuffer = new StringBuffer();
            for (int i = 1; i < node.getLevel(); i++) {
                stringBuffer.append("->");
            }
            menuSelect.setName(stringBuffer.toString() + node.getLabel());
            resultList.add(menuSelect);

            List<ElementTree> children = node.getChildren();
            Collections.reverse(children);

            if (children != null && !children.isEmpty()) {
                for (ElementTree child : children) {
                    nodeStack.push(child);
                }
            }
        }
        return resultList;
    }
}
