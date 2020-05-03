package com.zhuanbo.service.service;

/**
 * @title: ICipherService
 * @projectName mpmall.api
 * @description: 加解密处理
 * @date 2019/10/24 11:44
 */
public interface ICipherService {

    /**
     * AES解密
     *
     * @param content 密文
     * @return
     */
    String decryptAES(String content);

    /**
     * AES加密
     *
     * @param content 明文
     * @return
     */
    String encryptAES(String content);

}
