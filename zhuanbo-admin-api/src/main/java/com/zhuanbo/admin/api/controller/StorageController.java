package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.annotation.UnAuthAnnotation;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.storage.AliyunStorage;
import com.zhuanbo.core.storage.StorageService;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Order;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.core.constants.OSSPathEnum;
import com.zhuanbo.core.dto.AdminStorageDTO;
import com.zhuanbo.core.entity.Storage;
import com.zhuanbo.service.service.ICipherService;
import com.zhuanbo.service.service.IStorageService;
import com.zhuanbo.service.utils.LogOperateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 文件存储表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/storage")
@Slf4j
public class StorageController {

    /**
     * 上传类型
     */
    final String TYPE_OTHERS = "0";// 未知
    final String TYPE_AD = "1";// 广告
    final String TYPE_GOODS = "2";// 商品
    final String TYPE_DYNAMIC = "3";// 动态
    final String TYPE_GOODS_SPEC = "4";// 商品属性
    final String TYPE_INDEX = "5";// 首页图片
    final String TYPE_SHOWCATAGORY = "7";// 展示类目
    @Autowired
    private StorageService storageService;
    @Autowired
    private IStorageService iStorageService;
    @Autowired
    public AliyunStorage aliyunStorage;
    @Autowired
    public ICipherService iCipherService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_STORAGE_UPDATE = "lock_storage_update_";


    private String generateKey(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        String suffix = originalFilename.substring(index);

        String key = null;
        Storage storageInfo = null;

        do {
            key = CharUtil.getRandomString(20) + suffix;
            storageInfo = iStorageService.getOne(new QueryWrapper<Storage>().eq("storage_key", key));
        }
        while (storageInfo != null);

        return key;
    }

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       String key, String name,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        Page<Storage> pageCond = new Page<>(page, limit);
        QueryWrapper<Storage> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(key)) queryWrapper.eq("key", key);
        if (StringUtils.isNotBlank(name)) queryWrapper.like("name", name);
        IPage<Storage> storageIPage = iStorageService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", storageIPage.getTotal());
        data.put("items", storageIPage.getRecords());

        return ResponseUtil.ok(data);
    }

    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestParam("file") MultipartFile file, @RequestParam(value = "type", defaultValue = "0") String type) {
        LogOperateUtil.log("文件管理", "文件上传", null, adminId.longValue(), 0);
        if (adminId == null) {
            return ResponseUtil.unlogin();
        }
        boolean isPrivate = false;
        String originalFilename = file.getOriginalFilename();

        LocalDateTime now = LocalDateTime.now();
        String yyyyMMdd = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ossPath = null;
        if (TYPE_DYNAMIC.equals(type)) {
            ossPath = "dynamic/" + yyyyMMdd + "/";
        } else if (TYPE_AD.equals(type)) {
            ossPath = "ad/" + yyyyMMdd + "/";
        } else if (TYPE_GOODS.equals(type)) {
            ossPath = "goods/" + yyyyMMdd + "/";
        } else if (TYPE_GOODS_SPEC.equals(type)) {
            ossPath = "goodsspec/" + yyyyMMdd + "/";
        } else if (TYPE_INDEX.equals(type)) {
            ossPath = "index/" + yyyyMMdd + "/";
        } else if (TYPE_SHOWCATAGORY.equals(type)) {
            ossPath = "showCatagory/" + yyyyMMdd + "/";
        } else {
            ossPath = iStorageService.getOSSPath(originalFilename);
        }
        String key = ossPath + generateKey(originalFilename);
        storageService.store(file, key, isPrivate);

        String url = storageService.generateUrl(key);
        Storage storageInfo = new Storage();
        storageInfo.setName(originalFilename);
        storageInfo.setSize((int) file.getSize());
        storageInfo.setType(file.getContentType());
        storageInfo.setAddTime(LocalDateTime.now());
        storageInfo.setModified(LocalDateTime.now());
        storageInfo.setStorageKey(key);
        storageInfo.setUrl(url);
        iStorageService.save(storageInfo);
        return ResponseUtil.ok(storageInfo);
    }

    @PostMapping("/read")
    public Object read(@LoginAdmin Integer adminId, @RequestParam("id") Integer id) {
        if (adminId == null) {
            return ResponseUtil.unlogin();
        }
        Storage storageInfo = iStorageService.getById(id);
        if (storageInfo == null) {
            return ResponseUtil.badArgumentValue();
        }
        return ResponseUtil.ok(storageInfo);
    }

    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody Storage Storage) {
        LogOperateUtil.log("文件管理", "文件更新", null, adminId.longValue(), 0);
        if (adminId == null) {
            return ResponseUtil.unlogin();
        }
        String key = LOCK_STORAGE_UPDATE + Storage.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 30);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            iStorageService.updateById(Storage);
            return ResponseUtil.ok(Storage);
        } catch (Exception e) {
            throw e;
        } finally {
            if (b) {
                redissonLocker.unlock(key);
            }
        }

    }

    @PostMapping("/delete")
    public Object delete(@LoginAdmin Integer adminId, @RequestBody Storage Storage) {
        LogOperateUtil.log("文件管理", "文件删除", null, adminId.longValue(), 0);
        if (adminId == null) {
            return ResponseUtil.unlogin();
        }
        iStorageService.removeById(Storage.getId());
        storageService.delete(Storage.getStorageKey());
        return ResponseUtil.ok();
    }

    @GetMapping("/interior/ossResouces")
    @UnAuthAnnotation
    public void ossResoucesForInterior(@RequestParam("iconUrl") String url, HttpServletResponse response) throws Exception {
        iStorageService.ossResoucesForInterior(url, response);
    }

    @PostMapping("/stsToken")
    public Object getStsToken(@LoginAdmin Integer adminId, @RequestParam("iconUrl") String url) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("stsToken", aliyunStorage.getStsToken(url));
        return ResponseUtil.ok(map);
    }

    @PostMapping("/stsPostPolicy")
    public Object stsPostPolicy(@LoginAdmin Integer adminId, @RequestBody AdminStorageDTO dto) throws Exception {
        String type = dto.getType();
        String fileName = dto.getFileName();
        LocalDateTime now = LocalDateTime.now();
        String yyyyMMdd = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ossPath;
        OSSPathEnum oneBySourceAndType = OSSPathEnum.getOneBySourceAndType(1, type);
        if (null !=oneBySourceAndType) {
            ossPath  = oneBySourceAndType.getPath()+yyyyMMdd + "/";
        }else{
            ossPath = iStorageService.getOSSPath(fileName);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("ossPath",ossPath);
        map.put("fileName",generateKey(fileName));
        String s = iCipherService.encryptAES(JacksonUtil.objTojson(aliyunStorage.stsPostPolicy(map)));
        return ResponseUtil.ok(s);
    }

}
