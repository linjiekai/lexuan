package com.zhuanbo.client.server.client;

import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.dto.AdminUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Administrator
 * @title: YinLiAdminClient
 * @description: TODO
 * @date 2020/4/24 13:34
 */
@FeignClient(name = "yinli-admin-api")
public interface YinLiAdminClient {

    @PostMapping("/mpmall/admin/user/get/user/bymobileorauthno")
    ResponseDTO getUserByMobileAndAreaCode(@RequestBody AdminUserDTO adminUserDTO);

}
