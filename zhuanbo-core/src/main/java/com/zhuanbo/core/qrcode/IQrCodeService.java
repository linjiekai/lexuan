package com.zhuanbo.core.qrcode;

public interface IQrCodeService {
    /**
     * 生成二维码（有上传OSS）
     * @param content 内容
     * @param width 长
     * @param height 宽
     * @return
     */
    String make(String content, Integer width, Integer height);
}
