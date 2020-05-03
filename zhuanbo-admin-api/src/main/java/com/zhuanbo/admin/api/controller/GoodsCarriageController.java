package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.entity.GoodsCarriage;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IGoodsCarriageService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/admin/gc")
public class GoodsCarriageController {


    @Autowired
    private IGoodsCarriageService iGoodsCarriageService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_GC_UPDATE = "lock_goodsCarriage_update_";

    /**
     * 列表
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       GoodsCarriage goodsCarriage) {

        IPage<GoodsCarriage> iPage = new Page<>(page, limit);
        QueryWrapper<GoodsCarriage> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("update_time");
        if (goodsCarriage.getId() != null) {
            queryWrapper.eq("id", goodsCarriage.getId());
        }
        if (StringUtils.isNotBlank(goodsCarriage.getOperator())) {
            queryWrapper.eq("operator", goodsCarriage.getOperator());
        }

        IPage<GoodsCarriage> list = iGoodsCarriageService.page(iPage, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", list.getTotal());
        data.put("items", list.getRecords());
        return ResponseUtil.ok(data);
    }

    @GetMapping("/frist")
    public Object frist() {
        GoodsCarriage one = iGoodsCarriageService.getOne(new QueryWrapper<GoodsCarriage>());
        return ResponseUtil.ok(one);
    }

    /**
     * 添加
     *
     * @param adminId
     * @param goodsCarriage
     * @return
     */
//    @PostMapping("/create")
//    public Object create(@LoginAdmin Integer adminId, @RequestBody GoodsCarriage goodsCarriage) {
//        LogOperateUtil.log("商品运费", "创建", null, adminId.longValue(), 0);
//        if (null == goodsCarriage.getPrice()) {
//            return ResponseUtil.fail("11111", "缺少参数：price");
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        goodsCarriage.setAddTime(now);
//        goodsCarriage.setUpdateTime(now);
//        goodsCarriage.setOperator(iAdminService.getAdminName(adminId));
//        iGoodsCarriageService.save(goodsCarriage);
//
//        return list(adminId, 1, 1, goodsCarriage);
//    }

    /**
     * 详情
     *
     * @param adminId
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public Object detail(@LoginAdmin Integer adminId, @RequestParam Integer id) {
        return ResponseUtil.ok(iGoodsCarriageService.getById(id));
    }

    /**
     * 更新
     *
     * @param adminId
     * @param goodsCarriage
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody GoodsCarriage goodsCarriage) throws Exception {
        LogOperateUtil.log("商品运费", "修改", String.valueOf(goodsCarriage.getId()), adminId.longValue(), 0);
        if (null == goodsCarriage.getPrice()) {
            return ResponseUtil.fail("11111", "缺少参数：price");
        }

        LocalDateTime now = LocalDateTime.now();
        goodsCarriage.setUpdateTime(now);
        goodsCarriage.setOperator(iAdminService.getAdminName(adminId));
        updateByLock(goodsCarriage);

        return list(adminId, 1, 1, goodsCarriage);
    }

//    @PostMapping("/del")
//    public Object del(@LoginAdmin Integer adminId, @RequestBody GoodsCarriage goodsCarriage) throws Exception{
//        LogOperateUtil.log("商品运费", "删除", String.valueOf(goodsCarriage.getId()), adminId.longValue(), 0);
//        if (goodsCarriage.getId() != null) {
//            goodsCarriage = iGoodsCarriageService.getById(goodsCarriage.getId());
//            if (goodsCarriage != null) {
//                goodsCarriage.setDeleted(ConstantsEnum.DELETED_1.integerValue());
//                goodsCarriage.setUpdateTime(LocalDateTime.now());
//                goodsCarriage.setOperator(iAdminService.getAdminName(adminId));
//                updateByLock(goodsCarriage);
//            }
//        }
//        return ResponseUtil.ok();
//    }

    public void updateByLock(GoodsCarriage goodsCarriage) throws Exception {
        String lockKey = LOCK_GC_UPDATE + goodsCarriage.getId();
        boolean b = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 10, 30);
        if (!b) {
            throw new ShopException(30014);
        }
        try {
            iGoodsCarriageService.updateById(goodsCarriage);
        } catch (Exception e) {
            throw e;
        } finally {
            if (b) {
                redissonLocker.unlock(lockKey);
            }
        }
    }

}
