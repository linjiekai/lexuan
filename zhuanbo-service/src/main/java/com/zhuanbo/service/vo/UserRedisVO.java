package com.zhuanbo.service.vo;

import com.zhuanbo.core.entity.User;
import lombok.Data;

@Data
public class UserRedisVO extends User {
    private String password;
}
