package com.zhuanbo.client.server.client;

import com.zhuanbo.client.server.config.PayClientConfig;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.dto.PayCardBindDTO;
import com.zhuanbo.core.dto.PayDictionaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * pay服务
 */
@FeignClient(name = "pay-api",configuration = PayClientConfig.class)
public interface PayClient {

    /**
     * 统一入口
     *
     * @return
     */
    @PostMapping(value = "/mobile/unified",headers = {"content-type=application/json"})
    ResponseDTO unified(@RequestBody Map<String, Object> dto);

    /**
     * pay字典列表
     *
     * @return
     */
    @PostMapping(value = "/admin/dictionary/list",headers = {"content-type=application/json"})
    ResponseDTO dictionaryList(@RequestBody PayDictionaryDTO payDictionaryDTO);

    /**
     * pay字典分页
     *
     * @return
     */
    @PostMapping(value = "/admin/dictionary/page",headers = {"content-type=application/json"})
    ResponseDTO dictionaryPage(@RequestBody PayDictionaryDTO payDictionaryDTO);

    /**
     * pay字典更新
     *
     * @return
     */
    @PostMapping(value = "/admin/dictionary/update",headers = {"content-type=application/json"})
    ResponseDTO dictionaryUpdate(@RequestBody PayDictionaryDTO payDictionaryDTO);

    /**
     * pay字典更新
     *
     * @return
     */
    @PostMapping(value = "/admin/card/bind/get/cardbind",headers = {"content-type=application/json"})
    ResponseDTO getCardBindByAgrNo(@RequestBody PayCardBindDTO cardBindDTO);

}
