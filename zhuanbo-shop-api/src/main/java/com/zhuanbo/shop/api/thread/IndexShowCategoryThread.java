package com.zhuanbo.shop.api.thread;


import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.service.vo.ShowCategoryVO;

import java.util.List;
import java.util.concurrent.Callable;


public class IndexShowCategoryThread implements Callable<Object> {

    @Override
    public Object call() throws Exception {
        List<ShowCategoryVO> list= (List<ShowCategoryVO>) RedisUtil.get(ConstantsEnum.REDIS_INDEX_SHOWCATEGORY.stringValue());
        return list;
    }
}
