package com.zhuanbo.service.service;

import com.zhuanbo.core.dto.PayCardBindDTO;

/**
 * @author Administrator
 * @title: IPayCardBindService
 * @description: 支付卡绑定
 * @date 2020/4/15 15:56
 */
public interface IPayCardBindService {

    /**
     * 根据协议号查询绑定信息
     *
     * @param cardBind
     * @return
     */
    PayCardBindDTO getCardBindByAgrNo(PayCardBindDTO cardBind);
}
