package com.zhuanbo.service.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.storage.AliyunStorage;
import com.zhuanbo.core.entity.Storage;
import com.zhuanbo.service.mapper.StorageMapper;
import com.zhuanbo.service.service.IStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文件存储表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
@Slf4j
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements IStorageService {

    @Value("${storage.type-image}")
    private String typeImage;
    @Value("${storage.type-video}")
    private String typeVideo;
    @Value("${storage.type-image-folder}")
    private String typeImageFolder;
    @Value("${storage.type-video-folder}")
    private String typeVideoFolder;
    @Value("${storage.type-others-folder}")
    private String typeOthersFolder;
    @Autowired
    private AliyunStorage aliyunStorage;

    @Override
    public String getOSSPath(String fileName) {

        String s = "/";
        LocalDateTime now = LocalDateTime.now();
        String dateFormat = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        int lastDian = fileName.lastIndexOf(".");
        String sufName = fileName.substring(lastDian + 1).toUpperCase();
        List<String> sufNameList = Arrays.asList(typeImage.split(","));
        if (sufNameList.contains(sufName)) {
            return typeImageFolder + s + dateFormat + s;
        } else {
            sufNameList = Arrays.asList(typeVideo.split(","));
            if (sufNameList.contains(sufName)) {
                return typeVideoFolder + s + dateFormat + s;
            } else {
                return typeOthersFolder + s + sufName + s + dateFormat + s;
            }
        }
    }

    @Override
    public void ossResouces(String url, String token, HttpServletResponse response) throws Exception {
        boolean b = false;
        OutputStream outputStream = null;
        InputStream objectContent = null;
        OSS oss = null;
        try {
            if (StringUtils.isNotBlank(token)) {
                b = true;
            }
            Map<String, Object> map = aliyunStorage.getOssResouces(token, url, b);
            OSSObject ossResouces = (OSSObject) map.get("OSSObject");
            oss = (OSS) map.get("oss");
            if (ossResouces == null) {
                return;
            }
            response.setContentType(ossResouces.getObjectMetadata().getContentType());
            outputStream = response.getOutputStream();
            objectContent = ossResouces.getObjectContent();

            byte[] bytes = new byte[1024 * 10];
            int len = 0;
            while ((len = objectContent.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
        } catch (Exception e) {
            log.error("读取oss资源失败：{}", e);
            throw e;
        } finally {
            if (objectContent != null) {
                objectContent.close();
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (oss != null) {
                oss.shutdown();
            }

        }
    }

    @Override
    public void ossResoucesForInterior(String url, HttpServletResponse response) throws Exception {
        String stsToken = aliyunStorage.getStsToken(url);
        this.ossResouces(null, stsToken, response);
    }
}
