package com.zhuanbo.core.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 对象存储接口
 */
public interface Storage {

    /**
     * 存储一个文件对象
     * @param file      SpringBoot MultipartFile文件对象
     * @param keyName   文件索引名
     */
    void store(MultipartFile file, String keyName, boolean isPrivate);

    Stream<Path> loadAll();

    Path load(String keyName);

    Resource loadAsResource(String keyName);

    void delete(String keyName);

    String generateUrl(String keyName);

    /**
     * 按流来存
     * @param keyName
     * @param byteArrayInputStream
     */
    void storeBySteam(String keyName, ByteArrayInputStream byteArrayInputStream);

    String baseUrl();
}