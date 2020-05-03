package com.zhuanbo.core.constants;

import com.zhuanbo.core.util.MapUtil;

import java.util.Map;

public enum  NotifyPushEnum {
    /**399礼包完成通知*/
    GIFT_FINISH("系统通知", "恭喜你成为M达人，尊享自买省钱、分享赚钱，一件包邮的权益。邀请好友加入，还有额外收益哦~", MapUtil.of("type", 3, "link", "")),
    /**399礼包完成上级通知*/
    GIFT_FINISH_TO_PARENT("系统通知", "你邀请的好友 # 成为了M达人", MapUtil.of("type", 3, "link", "")),
    /**600礼包购买完成通知*/
    FOREVER_FINISH("系统通知", "恭喜你升级为M体验官", MapUtil.of("type", 3, "link", "")),
    /**600礼包购买完成上级通知*/
    FOREVER_FINISH_TO_PARENT("系统通知", "你邀请的好友 # 成为了M体验官", MapUtil.of("type", 3, "link", ""));

    private String title;
    private String content;
    private Map map;

    NotifyPushEnum(String title, String content, Map map) {
        this.title = title;
        this.content = content;
        this.map = map;
    }

    public String title() {
        return this.title;
    }

    public String content() {
        return this.content;
    }

    public Map map() {
        return this.map;
    }
}
