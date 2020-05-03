package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.client.server.client.YinLiAdminClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.dto.AdminUserDTO;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.service.service.IYinLiUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @title: YinLiUserServiceImpl
 * @description: TODO
 * @date 2020/4/24 13:02
 */
@Service
@Slf4j
public class YinLiUserServiceImpl implements IYinLiUserService {

    @Resource
    private YinLiAdminClient yinLiAdminClient;


    /**
     * 根据手机号, 区号获取用户信息
     *
     * @param adminUserDTO
     * @return
     */
    @Override
    public AdminUserDTO getUserByMobileAndAreaCode(AdminUserDTO adminUserDTO) {
        ResponseDTO responseDTO = yinLiAdminClient.getUserByMobileAndAreaCode(adminUserDTO);
        if (responseDTO == null) {
            log.error("|获取用力用户信息|失败|");
            throw new ShopException("获取用力用户信息失败");
        }
        String code = responseDTO.getCode();
        if (!Constants.SUCCESS_CODE.equals(code)) {
            log.error("|获取卡绑定信息|失败,code:{}, message:{}|", code, responseDTO.getMsg());
        }
        Object userDtoObj = responseDTO.getData();
        String userDtoStr = JSONObject.toJSONString(userDtoObj);
        JSONObject userDtoJson = JSONObject.parseObject(userDtoStr);
        adminUserDTO = JSONObject.toJavaObject(userDtoJson, AdminUserDTO.class);
        return adminUserDTO;
    }
}
