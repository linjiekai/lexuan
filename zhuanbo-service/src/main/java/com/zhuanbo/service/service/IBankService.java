package com.zhuanbo.service.service;

import java.util.List;
import java.util.Map;

/**
 * @title: IBankCode
 * @projectName mpmall.api
 * @description: 银行信息
 * @date 2019/10/24 15:03
 */
public interface IBankService {

    /**
     * 根据银行code获取银行信息
     * @param bankCodes
     * @return
     */
    List<Map<String,Object>> listByBankCodes(List<String> bankCodes) throws Exception;
}
