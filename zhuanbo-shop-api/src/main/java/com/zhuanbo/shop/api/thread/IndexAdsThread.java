package com.zhuanbo.shop.api.thread;


import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.service.vo.AdVO;

import java.util.List;
import java.util.concurrent.Callable;


public class IndexAdsThread implements Callable<Object> {

    @Override
    public Object call() throws Exception {
        List<AdVO> adList = (List<AdVO>) RedisUtil.get(ConstantsEnum.REDIS_INDEX_ADS.stringValue());
        return adList;
    }

}
