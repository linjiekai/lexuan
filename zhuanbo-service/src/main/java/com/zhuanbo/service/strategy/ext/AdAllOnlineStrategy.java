package com.zhuanbo.service.strategy.ext;

import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Ad;
import com.zhuanbo.core.entity.AdConfig;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.service.service.IAdService;
import com.zhuanbo.service.strategy.AdOnlineStrategy;
import com.zhuanbo.service.vo.AdVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @title: AllOnlineStrategy
 * @description: 广告上线策略:全部上线
 * @date 2019/11/1 13:53
 */
@Slf4j
@Component("adAllOnlineStrategy")
public class AdAllOnlineStrategy implements AdOnlineStrategy {

    @Autowired
    private IAdService adService;

    /**
     * 根据不同部位,操作不同广告上线策略
     *
     * @param ad
     * @param adConfig 广告上线配置信息
     * @return
     */
    @Override
    public void onlineHandle(Ad ad, AdConfig adConfig) {
        log.info("|广告策略|全部上线策略|当前处理广告:{}|", ad);
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
        if(1 == refreshRedisFlag){
            List<AdVO> adList = adService.getAdList(ad.getPosition(), ad.getPlatform());
            RedisUtil.set(ConstantsEnum.REDIS_PRIOR_ADS.stringValue(), adList, Constants.CACHE_EXP_TIME);
        }
    }
}
