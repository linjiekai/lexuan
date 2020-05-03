package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.tree.ElementTree;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.annotation.LoginDealersAdmin;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.AdminDealer;
import com.zhuanbo.core.entity.Permission;
import com.zhuanbo.core.entity.RolePermission;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.bcrypt.BCryptPasswordEncoder;
import com.zhuanbo.core.validator.Order;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.service.service.IAdminDealerService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IPermissionService;
import com.zhuanbo.service.service.IRolePermissionService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.utils.LogOperateUtil;
import com.zhuanbo.service.vo.AdminVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 管理员表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@Validated
@RequestMapping("/admin/dl/admin")
@Slf4j
public class AdminDealerController {

    @Autowired
    private IAdminDealerService adminDealerService;
//    @Autowired
//    private IRoleDealerService iRoleDealerService;
//    @Autowired
//    private IRolePermissionDealerService iRolePermissionDealerService;
//    @Autowired
//    private IPermissionDealerService iPermissionDealerService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    private IRolePermissionService iRolePermissionService;
    @Autowired
    private IPermissionService iPermissionService;
    private final static String LOCK_ADMIN_UPDATE = "lock_admin_dealer_update_";
    @GetMapping("/info")
    public Object info(String token){

        Map<String, Object> data = new HashMap<>();
        data.put("introduction", "Admin introduction");

        Object o = RedisUtil.get(token);
        if (o == null) {
            return ResponseUtil.badArgumentValue();
        }
        Integer tokenId = Integer.parseInt(o.toString());
        if(tokenId == null){
            return ResponseUtil.badArgumentValue();
        }

        Integer roleId=0;
        AdminDealer admin = adminDealerService.getById(tokenId);
        if(admin != null){
            roleId = iDictionaryService.findForLong("RoleId", "JXSAdmin").intValue();
            data.put("roles", Arrays.asList(roleId));
            data.put("admin", admin);
            data.put("name", admin.getUsername());
            data.put("avatar", admin.getAvatar());
            data.put("type", "admin");
        }else{
            User user = iUserService.getById(tokenId);
            if(null != user){
                //联创单独一个角色
                roleId =  iDictionaryService.findForLong("RoleId", "JXS").intValue();
                data.put("roles", Arrays.asList(roleId));
                data.put("user", user);
                data.put("name", user.getUserName());
                data.put("type", "user");

            }
        }

        // 权限树
        List<ElementTree> finalList = new ArrayList<>();

        if (roleId != null) {
            List<RolePermission> rolePermissionList = iRolePermissionService.list(new QueryWrapper<RolePermission>().eq("role_id", roleId));
            if (!rolePermissionList.isEmpty()) {

                List<Integer> permissionIds = rolePermissionList.stream().map(x -> x.getPermissionId()).collect(Collectors.toList());
                List<Permission> permissionList = iPermissionService.list(new QueryWrapper<Permission>()
                        .in("id", permissionIds).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));

                log.info("菜单目录：{}", permissionList);
                if (!permissionList.isEmpty()) {

                    Map<Integer, ElementTree> mapTmp = new HashMap<>();
                    for (Permission current : permissionList) {
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
            }
        }
        log.info("菜单目录perList：{}", finalList);
        data.put("perList", finalList);
        return ResponseUtil.ok(data);
    }

    @GetMapping("/list")
    public Object list(@LoginDealersAdmin Integer adminId,
                       String username,Integer id,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order){
        Page<AdminDealer> pageCond = new Page<>(page, limit);
        QueryWrapper<AdminDealer> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(sort);
        queryWrapper.eq("deleted", 0);
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username", username);
        }
        if(id != null){
            queryWrapper.eq("id", id);
        }
        IPage<AdminDealer> adminIPage  = adminDealerService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        List<AdminVO> adminVOList = new ArrayList<>();
        if (adminIPage.getRecords().size() > 0) {
//            List<RoleDealer> roleList = iRoleDealerService.list(null);
            AdminVO adminVO;
            for (AdminDealer record : adminIPage.getRecords()) {
                adminVO = new AdminVO();
                BeanUtils.copyProperties(record, adminVO);
                if (adminVO.getRoleId() != null && !adminVO.getRoleId().equals(0)) {}
//                if (!roleList.isEmpty()) {
//                    for (RoleDealer role : roleList) {
//                        if (role.getId().equals(adminVO.getRoleId())) {
//                            adminVO.setRoleName(role.getName());
//                            break;
//                        }
//                    }
//                }
                adminVOList.add(adminVO);
            }
        }
        data.put("total", adminIPage.getTotal());
        data.put("items", adminVOList);

        return ResponseUtil.ok(data);
    }

    @PostMapping("/create")
    public Object create(@LoginDealersAdmin Integer AdminId, @RequestBody AdminDealer admin){
        LogOperateUtil.log("管理员管理", "添加", null ,AdminId.longValue(), 0);
        String username = admin.getUsername();
        if(StringUtils.isBlank(username) || StringUtils.isBlank(admin.getPassword())){
            return ResponseUtil.badArgument();
        }
        if(StringUtils.isNotBlank(username)){
            QueryWrapper<AdminDealer> queryWrapper = new QueryWrapper<>();
//          queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
            queryWrapper.eq("username", username);
            AdminDealer ad = adminDealerService.getOne(queryWrapper);
            if(null != ad){
                if(ConstantsEnum.DELETED_1.integerValue() == ad.getDeleted()){
                    return ResponseUtil.fail(402, "管理员已经存在,且账号为删除状态,请联系管理员");
                }
                return ResponseUtil.fail(402, "管理员已经存在");
            }
        }

        String rawPassword = admin.getPassword();
        if(rawPassword == null || rawPassword.length() < 6){
            return ResponseUtil.fail(402, "管理员密码长度不能小于6");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(rawPassword);
        admin.setPassword(encodedPassword);
        admin.setAddTime(LocalDateTime.now());
        admin.setUpdateTime(admin.getAddTime());
        admin.setDeleted(ConstantsEnum.DELETED_0.integerValue());
        adminDealerService.save(admin);
        return list(AdminId,null, admin.getId(), 1, 1, "add_time", "desc");
    }

    @GetMapping("/read")
    public Object read(@LoginDealersAdmin String tokenUserId, @RequestParam Integer AdminId){
        AdminDealer Admin = adminDealerService.getById(AdminId);
        return ResponseUtil.ok(Admin);
    }

    @PostMapping("/update")
    public Object update(@LoginDealersAdmin Integer AdminId, @RequestBody AdminDealer admin){
        LogOperateUtil.log("管理员管理", "更新", String.valueOf(admin.getId()) ,AdminId.longValue(), 0);
        Integer anotherAdminId = admin.getId();
//        if(anotherAdminId.equals(1)){
//            return ResponseUtil.fail(10403, "超级管理员不能修改");
//        }
        String lockKey = LOCK_ADMIN_UPDATE+admin.getId();
        boolean b = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS,10, 30);
        if (!b) {
            return ResponseUtil.result(22001);
        }
        try{
            AdminDealer oldAdmin = adminDealerService.getById(admin.getId());
            if (oldAdmin != null) {
                if (StringUtils.isNotBlank(admin.getPassword())) {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    String encodedPassword = encoder.encode(admin.getPassword());
                    oldAdmin.setPassword(encodedPassword);
                }
                if (StringUtils.isNotBlank(admin.getAvatar())) {
                    oldAdmin.setAvatar(admin.getAvatar());
                }
                oldAdmin.setUpdateTime(LocalDateTime.now());
                oldAdmin.setRoleId(admin.getRoleId());
                adminDealerService.updateById(oldAdmin);
            } else {
                return ResponseUtil.fail(10403, "管理员不存在");
            }
            return list(AdminId,null, admin.getId(), 1, 1, "add_time", "desc");
        }catch(Exception e){
            throw e;
        }finally {
            if(b){
                redissonLocker.unlock(lockKey);
            }
        }


    }

    @PostMapping("/delete")
    public Object delete(@LoginDealersAdmin Integer AdminId, @RequestBody AdminDealer admin){
        LogOperateUtil.log("管理员管理", "删除", String.valueOf(admin.getId()) ,AdminId.longValue(), 0);
        Integer anotherAdminId = admin.getId();
//        if(anotherAdminId == 1){
//            return ResponseUtil.fail(10403, "超级管理员不能删除");
//        }
        adminDealerService.update(new AdminDealer(), new UpdateWrapper<AdminDealer>().set("deleted", ConstantsEnum.DELETED_1.integerValue()).eq("id", admin.getId()));
        return ResponseUtil.ok();
    }

    @GetMapping("/detail")
    public Object detail(@LoginAdmin Integer AdminId, AdminDealer admin){
        Integer anotherAdminId = admin.getId();
//        if(anotherAdminId.equals(1)){
//            return ResponseUtil.fail(10403, "超级管理员不能修改");
//        }
        return ResponseUtil.ok(adminDealerService.getById(admin.getId()));
    }

    @PostMapping("/token/info")
    public Object tokenInfo(@LoginDealersAdmin Integer adminId){
        Map<String, Object> map = new HashMap<>();
        map.put("id", adminId);

        String userName = null;
        AdminDealer admin = adminDealerService.getById(adminId);
        if(null != admin){
            map.put("type", "admin");
            map.put("username", admin.getUsername());
        }else{
            User user = iUserService.getById(adminId);
            if(null != user){
                map.put("type", "user");
                map.put("username", user.getUserName());
            }
        }

        return ResponseUtil.ok(map);
    }

}
