package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.IndexTopic;
import com.zhuanbo.core.entity.Quick;
import com.zhuanbo.service.mapper.ShopIndexTopicMapper;
import com.zhuanbo.service.service.IAdService;
import com.zhuanbo.service.service.IIndexTopicService;
import com.zhuanbo.service.service.IQuickService;
import com.zhuanbo.service.service.IShowCategoryService;
import com.zhuanbo.service.vo.TopicsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-03-14
 */
@Service
public class IndexTopicServiceImpl extends ServiceImpl<ShopIndexTopicMapper, IndexTopic> implements IIndexTopicService {

    @Autowired
    private IQuickService iQuickService;
    @Autowired
    private IAdService iAdService;
    @Autowired
    private IShowCategoryService iShowCategoryService;

    @Override
    public Page<TopicsVO> listTopics(Page<TopicsVO> page, String type, String goodsId) {
        page.setRecords(baseMapper.listTopics(page, type, goodsId));
        page.setTotal(baseMapper.getTopicsTotal(type, goodsId));
        return page;
    }

    @Override
    public void refreshAllCache() throws  Exception{
        //首页各种列表
        List<IndexTopic> indexTopicList = this.list(new QueryWrapper<IndexTopic>().eq("enable", true)
                .orderByDesc("sequence_number"));
        for (int i = 1; i < 4; i++) {
            List<IndexTopic> list = new LinkedList<>();
            for (IndexTopic item : indexTopicList) {
                if (i == item.getType().intValue()) {
                    list.add(item);
                }
            }
            //无论list是否为空，都存缓存
            RedisUtil.set(ConstantsEnum.REDIS_INDEX_TOPIC.stringValue() + i, list, Constants.CACHE_EXP_TIME);

        }
        //快捷入口
        List<Quick> quickList = iQuickService.list(new QueryWrapper<Quick>()
                .eq("status", 1).eq("deleted", 0).orderByDesc("indexs").last(" limit 8"));

        RedisUtil.set(ConstantsEnum.REDIS_INDEX_QUICK.stringValue(), !CollectionUtils.isEmpty(quickList)?quickList:new LinkedList<>() , Constants.CACHE_EXP_TIME);
        //广告缓存
        iAdService.saveAds2Redis();
        //展示分类
        iShowCategoryService.refreshIndexShowCategoryCache();

    }
}
