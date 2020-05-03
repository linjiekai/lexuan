package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.index.TopicTypeDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Order;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.IndexTopic;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IIndexTopicService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author rome
 * @since 2019-03-14
 */
@RestController
@RequestMapping("/admin/index")
public class IndexController {

    @Autowired
    private IIndexTopicService shopIndexService;

    @Autowired
    private IAdminService iAdminService;

    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_INDEX_UPDATE = "lock_index_update_";

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       String type,
                       String goodsId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        Page<IndexTopic> pageCond = new Page<>(page, limit);
        QueryWrapper<IndexTopic> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(type) && !"0".equals(type)) queryWrapper.eq("type", type);
        if (StringUtils.isNotBlank(goodsId)) queryWrapper.eq("goods_id", goodsId);
        IPage<IndexTopic> indexIPage = shopIndexService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", indexIPage.getTotal());
        data.put("items", indexIPage.getRecords());

        return ResponseUtil.ok(data);
    }

    @GetMapping("/goods/list")
    public Object goodsList(@LoginAdmin Integer adminId,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10000000") Integer limit,
                            @Sort @RequestParam(defaultValue = "add_time") String sort,
                            @Order @RequestParam(defaultValue = "desc") String order) {
        IPage<Goods> pageCond = new Page<>(page, limit);
        IPage<Goods> indexIPage = iGoodsService.getPartGoods(pageCond);
        Map<String, Object> data = new HashMap<>();
        data.put("total", indexIPage.getTotal());
        data.put("items", indexIPage.getRecords());

        return ResponseUtil.ok(data);
    }

    @GetMapping("/type/list")
    public Object typeList(@LoginAdmin Integer adminId) {

        Map<String, Object> data = new HashMap<>();
        String[] typList = {"普通列表", "橱窗列表", "横铺列表"};
        List<TopicTypeDTO> topicTypeDTOList = new ArrayList<>();
        for (int i = 0; i < typList.length; i++) {
            TopicTypeDTO topicTypeDTO = new TopicTypeDTO();
            topicTypeDTO.setType(i + 1);
            topicTypeDTO.setName(typList[i]);
            topicTypeDTOList.add(topicTypeDTO);
        }

        data.put("total", typList.length);
        data.put("items", topicTypeDTOList);
        return ResponseUtil.ok(data);
    }


    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody IndexTopic indexTopic) {
        LogOperateUtil.log("首页管理", "添加", null, adminId.longValue(), 0);
        IndexTopic one = shopIndexService.getOne(new QueryWrapper<IndexTopic>()
                .eq("type", indexTopic.getType())
                .eq("sequence_number", indexTopic.getSequenceNumber()));
        if (one != null) {
            return ResponseUtil.result(71007);
        }
        indexTopic.setOperator(iAdminService.getAdminName(adminId));
        indexTopic.setUpdateTime(LocalDateTime.now());
        shopIndexService.save(indexTopic);
        return ResponseUtil.ok(indexTopic);
    }

    /**
     * @param adminId
     * @param indexTopic
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody IndexTopic indexTopic) {
        LogOperateUtil.log("首页管理", "更新", String.valueOf(indexTopic.getId()), adminId.longValue(), 0);
        String lockKey = LOCK_INDEX_UPDATE + indexTopic.getId();
        boolean lock = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 5, 30);
        if (!lock) {
            return ResponseUtil.result(30014);
        }
        try {
            IndexTopic one = shopIndexService.getOne(new QueryWrapper<IndexTopic>()
                    .eq("type", indexTopic.getType())
                    .eq("sequence_number", indexTopic.getSequenceNumber()));
            if (one != null && !one.getId().equals(indexTopic.getId())) {
                return ResponseUtil.result(71007);
            }
            indexTopic.setOperator(iAdminService.getAdminName(adminId));
            indexTopic.setUpdateTime(LocalDateTime.now());
            shopIndexService.updateById(indexTopic);
            return ResponseUtil.ok(indexTopic);
        } catch (Exception e) {
            throw e;
        } finally {
            if(lock){
                redissonLocker.unlock(lockKey);
            }

        }
    }

    @PostMapping("/delete")
    public Object delete(@LoginAdmin Integer adminId, @RequestBody IndexTopic indexTopic) {
        LogOperateUtil.log("首页管理", "删除", String.valueOf(indexTopic.getId()), adminId.longValue(), 0);
        shopIndexService.removeById(indexTopic);
        return ResponseUtil.ok();
    }

    @PostMapping("/refresh")
    public Object refresh(@LoginAdmin Integer adminId) throws Exception{
        shopIndexService.refreshAllCache();
        return ResponseUtil.ok();
    }
}
