package com.zhuanbo.service.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 登录返回数据DTO
 */
@Data
public class MPLoginOrRegisterVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private UserLoginVO userLoginVO;
    private Map<String, Object> registerMap;
}
