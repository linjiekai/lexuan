package com.zhuanbo.shop.api.thread;


import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Quick;
import com.zhuanbo.core.util.RedisUtil;

import java.util.List;
import java.util.concurrent.Callable;


public class IndexQuickThread implements Callable<Object> {

    @Override
    public Object call() throws Exception {
        List<Quick> quickList = (List<Quick>) RedisUtil.get(ConstantsEnum.REDIS_INDEX_QUICK.stringValue());
        return quickList;
    }
}
