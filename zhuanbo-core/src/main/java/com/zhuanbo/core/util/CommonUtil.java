package com.zhuanbo.core.util;

import java.util.UUID;

/**
 * Created by rome.chen on 2018/10/23.
 */
public class CommonUtil {

    public static String getUUID32(){
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        return uuid;
    }
}
