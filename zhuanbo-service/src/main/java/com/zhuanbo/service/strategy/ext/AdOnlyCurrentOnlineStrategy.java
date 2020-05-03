package com.zhuanbo.service.strategy.ext;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Ad;
import com.zhuanbo.core.entity.AdConfig;
import com.zhuanbo.core.enums.AdStatusEnum;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.service.service.IAdService;
import com.zhuanbo.service.strategy.AdOnlineStrategy;
import com.zhuanbo.service.vo.AdVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @title: AdOnlyOneOnlineStrategy
 * @description: 当前设置广告生效, 其余广告失效
 * @date 2019/11/1 14:02
 */
@Slf4j
@Component("adOnlyCurrentOnlineStrategy")
public class AdOnlyCurrentOnlineStrategy implements AdOnlineStrategy {

    @Autowired
    private IAdService adService;

    /**
     * 当前设置广告生效, 其余广告失效
     *
     * @param ad
     * @param adConfig 广告上线配置信息
     * @return
     */
    @Override
    public void onlineHandle(Ad ad, AdConfig adConfig) {
        log.info("|广告策略|当前新增广告生效|当前处理广告:{}|", ad);
        LocalDateTime now = LocalDateTime.now();
        List<Ad> oldAdList = adService.list(new QueryWrapper<Ad>().select("id")
                .eq("platform", ad.getPlatform())
                .eq("status", AdStatusEnum.ON_LINE.getId())
                .eq("deleted", false)
                .eq("position", ad.getPosition())
                .le("start_time", now)
                .ge("end_time", now)
                .ne("id", ad.getId()));
        List<Integer> ids = oldAdList.stream().map(oldAd -> oldAd.getId()).collect(toList());
        if (!CollectionUtils.isEmpty(ids)) {
            adService.update(new Ad(), new UpdateWrapper<Ad>().set("status", AdStatusEnum.OFF_LINE.getId()).in("id", ids));
        }
    }

    /**
     * 刷新redis缓存
     *
     * @param ad       广告信息
     * @param adConfig 广告上线配置信息
     */
    @Override
    public void refreshAdRedis(Ad ad, AdConfig adConfig) {
        Integer refreshRedisFlag = adConfig.getRefreshRedisFlag();
        if (1 == refreshRedisFlag) {
            List<AdVO> adList = adService.getAdList(ad.getPosition(), ad.getPlatform());
            RedisUtil.set(ConstantsEnum.REDIS_PRIOR_ADS.stringValue(), adList, Constants.CACHE_EXP_TIME);
        }
    }
}
