package com.zhuanbo.service.service;

import com.zhuanbo.service.vo.TransCodeVO;

public interface IVideoTransCodeService {
    /**
     * 视频转码
     * @param transCodeVO
     */
    TransCodeVO transCode(TransCodeVO transCodeVO) throws Exception;

    /**
     * 异步转码MQ
     * @param object
     */
    void sendTrans(Object object);
}
