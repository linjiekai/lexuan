package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.annotation.UnAuthAnnotation;
import com.zhuanbo.core.constants.OSSPathEnum;
import com.zhuanbo.core.dto.QrCodeDTO;
import com.zhuanbo.core.entity.Storage;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.qrcode.IQrCodeService;
import com.zhuanbo.core.storage.AliyunStorage;
import com.zhuanbo.core.storage.StorageService;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文件存储表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Slf4j
@RestController
@RequestMapping("/shop/mobile/storage")
public class MobileStorageController {

    @Autowired
    private StorageService storageService;
    @Autowired
    private IStorageService iStorageService;
    @Autowired
    private AliyunStorage aliyunStorage;
    @Autowired
    private IQrCodeService iQrCodeService;

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

    @PostMapping("/create")
    public Object create(@LoginUser Long userId, @RequestParam("files") MultipartFile[] files, @RequestParam("type") String type) {

        boolean isPrivate = false;
        if (files != null && files.length > 0) {
            // 保存到OSS的路径前缀
            String ossPath;
            OSSPathEnum oneBySourceAndType = OSSPathEnum.getOneBySourceAndType(0, type);
            if (null ==oneBySourceAndType) {
                log.error("|图片上传|获取文件路径|失败|未配置当前类型文件路径|");
                throw new ShopException(41004);
            }
            ossPath = oneBySourceAndType.getPath();
            isPrivate = oneBySourceAndType.isPrivate();
            List<String> urls = new ArrayList<>();
            String originalFilename = null;
            String key = null;
            String url = null;
            Storage storageInfo = null;
            LocalDateTime now = LocalDateTime.now();
            for (MultipartFile f : files) {

                originalFilename = f.getOriginalFilename();
                if (StringUtils.isBlank(ossPath)) {
                    ossPath = iStorageService.getOSSPath(originalFilename);
                }
                key = ossPath + generateKey(originalFilename);
                storageService.store(f, key, isPrivate);

                url = storageService.generateUrl(key);
                storageInfo = new Storage();
                storageInfo.setName(originalFilename);
                storageInfo.setSize((int) f.getSize());
                storageInfo.setType(f.getContentType());
                storageInfo.setAddTime(now);
                storageInfo.setModified(now);
                storageInfo.setStorageKey(key);
                storageInfo.setUrl(url);
                iStorageService.save(storageInfo);
                urls.add(url);
            }
            return ResponseUtil.ok(urls);
        }
        return ResponseUtil.fail();
    }

    @PostMapping("/stsToken")
    public Object getStsToken(@LoginUser Long userId, @RequestParam("iconUrl") String url) throws Exception {
        Map<String,String> map = new HashMap<>();
        map.put("stsToken",aliyunStorage.getStsToken(url));
        return ResponseUtil.ok(map);
    }

    @GetMapping("/interior/ossResouces")
    @UnAuthAnnotation
    public void ossResoucesForInterior(@RequestParam("iconUrl") String url, HttpServletResponse response) throws Exception {
        iStorageService.ossResoucesForInterior( url, response);
    }


    @GetMapping("/ossResouces")
    @UnAuthAnnotation
    public void ossResouces(@RequestParam("stsToken") String token, HttpServletResponse response) throws Exception {
        iStorageService.ossResouces(null, token, response);
    }

    @PostMapping("/stsUploadPolicyForApp")
    public Object stsUploadPolicyForApp(@LoginUser Long userId, @RequestParam("type") String type, @RequestParam("isPrivate") int isPrivate) throws Exception {
        boolean b = isPrivate == 0 ? false : true;
        OSSPathEnum oneBySourceAndType = OSSPathEnum.getOne(0, type, b);
        return ResponseUtil.ok(aliyunStorage.stsUploadPolicyForApp(oneBySourceAndType.getPath()));
    }

    /**
     * 二维码生成
     * @param userId
     * @param qrCodeDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/qrcode/make")
    public Object qrcodeMake(@LoginUser Long userId, @RequestBody QrCodeDTO qrCodeDTO) throws Exception {
        String make = iQrCodeService.make(qrCodeDTO.getContent(), qrCodeDTO.getWidth(), qrCodeDTO.getHeight());
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("url", make);
        return ResponseUtil.ok(stringObjectHashMap);
    }
}
