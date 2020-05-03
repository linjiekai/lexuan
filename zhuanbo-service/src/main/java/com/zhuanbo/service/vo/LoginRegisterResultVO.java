package com.zhuanbo.service.vo;

import com.zhuanbo.core.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class LoginRegisterResultVO implements Serializable {

    private static final long serialVersionUID = -7251090737635293125L;
    private User user;
    private User inviter;
    private List<NotifyPushMQVO> notifyPushMQVOList;
    private List<Map<String, Object>> mqList;
    private UserLoginVO userLoginVO;
    private String unionidStr;
    private String sessionKeyStr;
}
