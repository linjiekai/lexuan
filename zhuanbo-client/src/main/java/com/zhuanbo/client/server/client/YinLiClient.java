package com.zhuanbo.client.server.client;

import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.client.server.dto.common.YLUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 请求引力
 */
@FeignClient(name = "yinli-shop-api")
public interface YinLiClient {

    @PostMapping("/client/user/findByMobile")
    ResponseDTO findByMobile(@RequestBody YLUserDTO ylUserDTO);
}
