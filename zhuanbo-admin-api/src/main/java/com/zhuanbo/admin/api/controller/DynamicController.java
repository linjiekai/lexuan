package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.dynamic.DynamicDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.entity.Dynamic;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.service.*;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 广告表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/dynamic")
public class DynamicController {

    /**
     * 视频截帧：xxx.mp4?x-oss-process=video/snapshot,t_1,f_png
     */
    private final static String LOCK_DYNAMIC_UPDATE = "lock_dynamic_update_";

    @Autowired
    private IDynamicService iDynamicService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    @Autowired
    private IVideoTransCodeService iVideoTransCodeService;

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       Dynamic dynamic,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit) {

        Page<Dynamic> pageCond = new Page<>(page, limit);
        QueryWrapper<Dynamic> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sequence_number");
        queryWrapper.eq("deleted", 0);
        Optional.ofNullable(dynamic.getId()).ifPresent(x -> queryWrapper.eq("id", x));
        if (StringUtils.isNotBlank(dynamic.getContent())) {
            queryWrapper.like("content", "%" + dynamic.getContent() + "%");
        }

        IPage<Dynamic> iPage = iDynamicService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());

        List<DynamicDTO> dynamicDTOList = new ArrayList<>();
        if (iPage.getRecords().size() > 0) {

            User user;
            Goods goods;
            DynamicDTO dynamicDTO = null;
            for (Dynamic d : iPage.getRecords()) {// 获取作者名和商品名
                dynamicDTO = new DynamicDTO();
                BeanUtils.copyProperties(d, dynamicDTO);
                user = iUserService.getById(d.getUserId());
                if (user != null) {
                    dynamicDTO.setUserName(user.getNickname());
                }
                if (d.getGoodsId() != null) {
                    goods = iGoodsService.getById(d.getGoodsId());
                    if (goods != null) {
                        dynamicDTO.setGoodsName(goods.getName());
                    }
                }
                dynamicDTOList.add(dynamicDTO);
            }
        }
        data.put("items", dynamicDTOList);
        return ResponseUtil.ok(data);
    }

    /**
     * 增
     *
     * @param dynamic
     * @return
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody Dynamic dynamic) throws Exception {

        LogOperateUtil.log("动态管理", "创建", null, adminId.longValue(), 0);
        if (dynamic.getUserId() == null) {
            return ResponseUtil.fail("11111", "缺少参数:userId");
        }
        if (StringUtils.isBlank(dynamic.getVideoUrl())) {
            return ResponseUtil.fail("11111", "缺少参数:videoUrl");
        }
        Dynamic one = iDynamicService.getOne(new QueryWrapper<Dynamic>().eq("sequence_number", dynamic.getSequenceNumber()).eq("deleted", false));
        if (one != null) {
            return ResponseUtil.fail("11111", "序号已存在，不能重复");
        }
        LocalDateTime now = LocalDateTime.now();
        dynamic.setAddTime(now);
        dynamic.setUpdateTime(now);
        dynamic.setShowTime(Optional.ofNullable(dynamic.getShowTime()).orElse(now));
        dynamic.setOperater(iAdminService.getAdminName(adminId));
        dynamic.setLikeNumber(0);
        // 视频长和高
        BufferedImage read = ImageIO.read(new URL(dynamic.getVideoUrl() + "?x-oss-process=video/snapshot,t_1,f_png"));
        dynamic.setVideoWidth(read.getWidth());
        dynamic.setVideoHeight(read.getHeight());
        iDynamicService.save(dynamic);

        // 转码
        iVideoTransCodeService.sendTrans(dynamic);

        Dynamic d = new Dynamic();
        d.setId(dynamic.getId());
        return list(adminId, d, 1, 1);
    }

    /**
     * 查
     *
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        return ResponseUtil.ok(iDynamicService.getById(id));
    }

    /**
     * 改
     *
     * @param dynamic
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody Dynamic dynamic) {
        LogOperateUtil.log("动态管理", "修改", String.valueOf(dynamic.getId()), adminId.longValue(), 0);
        String lockKey = LOCK_DYNAMIC_UPDATE + dynamic.getId();
        boolean b = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 10, 30);
        if (!b) {
            return ResponseUtil.result(22001);
        }
        try {
            Dynamic one = iDynamicService.getOne(new QueryWrapper<Dynamic>().eq("sequence_number", dynamic.getSequenceNumber()).eq("deleted", false));
            if (one != null && !one.getId().equals(dynamic.getId())) {
                return ResponseUtil.fail("11111", "序号已存在，禁止重复");
            }
            boolean transCode = !one.getVideoUrl().equals(dynamic.getVideoUrl());
            dynamic.setVideoId(one.getVideoId());
            dynamic.setVideoTranscodeUrl(one.getVideoTranscodeUrl());
            dynamic.setUpdateTime(LocalDateTime.now());
            dynamic.setOperater(iAdminService.getAdminName(adminId));
            iDynamicService.updateById(dynamic);
            // 转码
            if (transCode) {
                iVideoTransCodeService.sendTrans(dynamic);
            }
            Dynamic d = new Dynamic();
            d.setId(dynamic.getId());
            return list(adminId, d, 1, 1);
        } catch (Exception e) {
            throw e;
        } finally {
            redissonLocker.unlock(lockKey);
        }
    }
}
