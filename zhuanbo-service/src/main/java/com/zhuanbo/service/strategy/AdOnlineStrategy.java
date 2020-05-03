package com.zhuanbo.service.strategy;


import com.zhuanbo.core.entity.Ad;
import com.zhuanbo.core.entity.AdConfig;

/**
 * @title: AdOnlineStrategy
 * @description: 广告上线策略
 * @date 2019/11/1 10:39
 */
public interface AdOnlineStrategy {

    /**
     * 根据不同部位,操作不同广告上线策略
     *
     * @param ad       广告信息
     * @param adConfig 广告上线配置信息
     */
    void onlineHandle(Ad ad, AdConfig adConfig);

    /**
     * 刷新redis缓存
     *
     * @param ad       广告信息
     * @param adConfig 广告上线配置信息
     */
    void refreshAdRedis(Ad ad, AdConfig adConfig);
}
