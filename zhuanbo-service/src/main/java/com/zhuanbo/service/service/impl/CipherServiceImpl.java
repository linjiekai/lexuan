package com.zhuanbo.service.service.impl;

import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.AESCoder;
import com.zhuanbo.service.service.ICipherService;
import com.zhuanbo.service.service.IDictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @title: CipherServiceImpl
 * @projectName mpmall.api
 * @description: 加解密处理
 * @date 2019/10/24 11:45
 */
@Service
@Slf4j
public class CipherServiceImpl implements ICipherService {

    @Autowired
    private IDictionaryService dictionaryService;

    /**
     * AES解密
     *
     * @param content 密文
     * @return
     */
    @Override
    public String decryptAES(String content) {
        String aesKey = dictionaryService.findForString("SecretKey", "AES");
        String aesIv = dictionaryService.findForString("SecretKey", "IV");

        try {
            content = AESCoder.decrypt(content, aesKey, aesIv);
        } catch (Exception e) {
            log.error("解密失败， content={}", content);
            throw new ShopException(13110);
        }
        return content;
    }

    /**
     * AES加密
     *
     * @param content 明文
     * @return
     */
    @Override
    public String encryptAES(String content) {
        String aesKey = dictionaryService.findForString("SecretKey", "AES");
        String aesIv = dictionaryService.findForString("SecretKey", "IV");

        try {
            content = AESCoder.encrypt(content, aesKey, aesIv);
        } catch (Exception e) {
            log.error("加密失败， content={}", content);
            throw new ShopException(13110);
        }

        return content;
    }

}
