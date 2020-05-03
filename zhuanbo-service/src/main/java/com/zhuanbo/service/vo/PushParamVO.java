package com.zhuanbo.service.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PushParamVO {
    private String type;// broadcast、customizedcast
    private String aliasType;// alias、fileId
    private String alias;// 1,2,3
    private String fileId;
    private Integer productionMode;// 1：正式、0：测试
    private String title;
    private String startTime;
    private String description;
    private Map<String, Object> extra;
    // 安卓特有
    private String ticker;// 通知栏提示文字
    private String text;// 通知文字描述
    // IOS特有
    private String subtitle;// 副标题
    private String body;// 内容

    private String mipush;
    private String miActivity;

    private String afterOpen;
    private String url;
    private String activity;
    private String platform = "ZBMALL";
}
