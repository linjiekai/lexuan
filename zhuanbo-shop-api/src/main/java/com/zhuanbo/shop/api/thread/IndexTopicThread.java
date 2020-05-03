package com.zhuanbo.shop.api.thread;


import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.IndexTopic;
import com.zhuanbo.core.util.RedisUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


public class IndexTopicThread implements Callable<Object> {

    @Override
    public Object call() throws Exception {
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<IndexTopic> indexTopicList = null;

        for (int type = 1; type < 4; type++) {
            indexTopicList = (List<IndexTopic>) RedisUtil.get(ConstantsEnum.REDIS_INDEX_TOPIC.stringValue() + type);

            if (null == indexTopicList) {
                indexTopicList = new ArrayList<IndexTopic>();
            }
            Map<String, Object> data = new HashMap<>();
            data.put("type", type);
            data.put("items", indexTopicList);
            mapList.add(data);
        }
        return mapList;
    }
}
