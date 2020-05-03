package com.zhuanbo.service.service;

import java.util.concurrent.Future;

public interface IThreadService {
    /**
     * 某用户团队数量
     * @param id 用户id
     * @param ptLevel 用户等级
     * @return
     */
    Future<Long> treeCount(String id, Integer ptLevel);
}
