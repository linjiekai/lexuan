package com.zhuanbo.service.service.impl;

import com.zhuanbo.service.service.IThreadService;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
public class ThreadServiceImpl implements IThreadService {

   /* @Autowired
    private IGraphService iGraphService;*/

    @Override
    public Future<Long> treeCount(String id, Integer ptLevel) {
        return new AsyncResult<>(0L);
    }
}
