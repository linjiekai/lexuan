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
import java.util.LinkedList;
import java.util.List;

/**
 * @description: 保留规定数量上线广告, 其余广告下线
 * @date 2019/11/1 14:01
 */
@Slf4j
@Component("adCountOnlineStrategy")
public class AdCountOnlineStrategy implements AdOnlineStrategy {

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
        log.info("|广告策略|保留规定上线数量|当前处理广告:{}|", ad);
        LocalDateTime now = LocalDateTime.now();
        int onlineCount = adConfig.getOnlineCount();
        List<Ad> adList = adService.list(new QueryWrapper<Ad>().select("id")
                .eq("platform", ad.getPlatform())
                .eq("status", AdStatusEnum.ON_LINE.getId())
                .eq("deleted", false)
                .le("start_time", now)
                .eq("position", ad.getPosition())
                .ge("end_time", now)
                .orderByDesc("sequence_number"));
        // 批量更新下,onlineCount 条以后的置为失效
        if (!CollectionUtils.isEmpty(adList) && adList.size() > onlineCount) {
            List<Integer> ids = new LinkedList<>();
            for (int i = onlineCount; i < adList.size(); i++) {
                Integer id = adList.get(i).getId();
                ids.add(id);
            }
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
        if(1 == refreshRedisFlag){
            List<AdVO> adList = adService.getAdList(ad.getPosition(), ad.getPlatform());
            RedisUtil.set(ConstantsEnum.REDIS_PRIOR_ADS.stringValue(), adList, Constants.CACHE_EXP_TIME);
        }
    }
}
