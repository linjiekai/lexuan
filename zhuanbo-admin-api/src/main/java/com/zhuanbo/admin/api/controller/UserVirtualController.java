package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.headImage.HeaderImageDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.HeadImg;
import com.zhuanbo.core.entity.UserVirtual;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IHeadImgService;
import com.zhuanbo.service.service.IUserVirtualService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/user/virtual")
public class UserVirtualController {

    @Autowired
    private IUserVirtualService iUserVirtualService;
    @Autowired
    private IHeadImgService iHeadImgService;
    @Autowired
    private IAdminService iAdminService;


    /**
     * 列表
     * @param page
     * @param limit
     * @param sort
     * @param userVirtual
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       UserVirtual userVirtual) {


        QueryWrapper<UserVirtual> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(sort);
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());

        List<UserVirtual> list = iUserVirtualService.list(queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", 1);
        LocalDateTime addTime = null;
        String operator = null;
        String nickName = null;
        if (list.size() > 0) {
            UserVirtual uv = list.get(0);
            addTime = uv.getAddTime();
            operator = uv.getOperator();
            nickName = list.stream().map(x -> x.getNickname()).collect(Collectors.joining(","));
        }
        // 头像
        LocalDateTime headImageUploadTime = null;
        List<HeadImg> headImgList = iHeadImgService.list(new QueryWrapper<>());
        List<String> headImageList = headImgList.stream().map(x -> x.getHeadImgUrl()).collect(Collectors.toList());
        if (!headImgList.isEmpty()) {
            HeadImg headImg = headImgList.get(0);
            headImageUploadTime = headImg.getAddTime();
        }
        data.put("items", Arrays.asList(MapUtil.of("addTime", addTime, "operator", operator, "nickName", nickName,
                "headImageList", headImageList, "headImageUploadTime", headImageUploadTime)));
        return ResponseUtil.ok(data);
    }

    /**
     * 增
     * @return
     */
    @Transactional
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody UserVirtual userVirtual) {

        LogOperateUtil.log("虚拟用户管理", "创建", null, adminId.longValue(), 0);
        if (StringUtils.isBlank(userVirtual.getNickname())) {
            return ResponseUtil.fail("11111", "缺少参数：nickname");
        }
        String nickName = userVirtual.getNickname();
        String[] split = nickName.split("\\|");
        if (split.length > 0) {
            // 软删之前的
            iUserVirtualService.update(new UserVirtual(), new UpdateWrapper<UserVirtual>().set("deleted", ConstantsEnum.DELETED_1.integerValue()));
            // 保存现在的
            UserVirtual uv;
            String operator = iAdminService.getAdminName(adminId);
            LocalDateTime now = LocalDateTime.now();
            List<UserVirtual> userVirtualList = new ArrayList<>();

            Integer ptLevel = 0;
            for (String s : split) {
            	ptLevel = (int) (Math.random() * 10);
            	if (ptLevel == 0) {
            		ptLevel = 1;
            	}
                uv = new UserVirtual();
                uv.setNickname(s);
                uv.setPtLevel(ptLevel);
                uv.setOperator(operator);
                uv.setDeleted(ConstantsEnum.DELETED_0.integerValue());
                uv.setAddTime(now);
                uv.setUpdateTime(now);
                userVirtualList.add(uv);
            }
            iUserVirtualService.saveBatch(userVirtualList);
        }
        return ResponseUtil.ok();
    }


    /**
     * 虚拟头像列表
     * @param adminId
     * @return
     */
    @GetMapping("/header/list")
    public Object headList(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer limit) {


        Page<HeadImg> pageCond = new Page<>(page, limit);
        QueryWrapper<HeadImg> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        IPage<HeadImg> headImgIPage = iHeadImgService.page(pageCond, queryWrapper);
        Map<String, Object> backMap = new HashMap<>();
        backMap.put("total", headImgIPage.getTotal());
        backMap.put("items", headImgIPage.getRecords());
        return ResponseUtil.ok(backMap);
    }

    /**
     * 虚拟头像创建
     * @param adminId
     * @param headerImageReqDTO
     * @return
     */
    @Transactional
    @PostMapping("/header/create")
    public Object headCreate(@LoginAdmin Integer adminId, @RequestBody HeaderImageDTO headerImageReqDTO) {
        if (headerImageReqDTO.getHeaderImages() == null || headerImageReqDTO.getHeaderImages().size() == 0) {
            return ResponseUtil.fail("11111", "缺少参数：headerImages");
        }
        // 硬删之前的
        iHeadImgService.remove(new QueryWrapper<>());

        List<String> headerImages = headerImageReqDTO.getHeaderImages();
        if (headerImages.size() > 0) {
            List<HeadImg> headImgList = headerImages.stream().map(x -> {
                HeadImg headImg = new HeadImg();
                headImg.setHeadImgUrl(x);
                return headImg;
            }).collect(Collectors.toList());
            iHeadImgService.saveBatch(headImgList);
        }
        return ResponseUtil.ok();
    }

}
