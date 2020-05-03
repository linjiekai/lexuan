package com.zhuanbo.service.service;

import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.dto.PayDictionaryDTO;
import com.zhuanbo.core.dto.WithdrDicDTO;

import java.util.List;

/**
 * @author Administrator
 * @title: IPayDictionaryService
 * @date 2020/4/1 11:14
 */
public interface IPayDictionaryService {

    /**
     * mppay 字典表数据
     *
     * @param payDictionaryDTO
     * @return
     */
    List<PayDictionaryDTO> list(PayDictionaryDTO payDictionaryDTO);

    /**
     * mppay 字典表分页查询
     *
     * @param payDictionaryDTO
     * @return
     */
    ResponseDTO page(PayDictionaryDTO payDictionaryDTO);

    /**
     * mppay 字典表分页更新
     *
     * @param payDictionaryDTO
     * @return
     */
    ResponseDTO update(PayDictionaryDTO payDictionaryDTO);

    /**
     * 获取提现字典数据
     * @return
     */
    WithdrDicDTO getWithdrDic();

}
