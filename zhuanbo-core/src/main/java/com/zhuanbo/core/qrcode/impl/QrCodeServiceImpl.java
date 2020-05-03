package com.zhuanbo.core.qrcode.impl;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.qrcode.IQrCodeService;
import com.zhuanbo.core.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class QrCodeServiceImpl implements IQrCodeService {

    public static final String PNG = "png";

    @Autowired
    private StorageService storageService;

    @Override
    public String make(String content, Integer width, Integer height) {
        if (width == null) {
            width = 300;
        }
        if (height == null) {
            height = 300;
        }
        String keyName = "mp-qr-code/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/" + UUID.randomUUID().toString() + "." + PNG;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayInputStream byteArrayInputStream = null;

        try {
            QrCodeUtil.generate(content, width, height, PNG, byteArrayOutputStream);
            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            storageService.storeByStream(keyName, byteArrayInputStream);
        } catch (Exception e) {
            log.error("上传二维码流失败");
            throw new ShopException("上传二维码失败");
        } finally {
            try {
                byteArrayOutputStream.close();
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return storageService.baseUrl() + "/" + keyName;
    }
}
