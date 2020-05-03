package com.zhuanbo.external.service.wx.vo;

import lombok.Data;

@Data
public class ShareTicketThirdVO {
    private String appId;
    private Integer timestamp;//必填，生成签名的时间戳
    private String nonceStr;// 必填，生成签名的随机串
    private String signature;// 必填，签名，见附录1
}
