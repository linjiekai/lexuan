package com.zhuanbo.shop.api.dto.req;


import lombok.Data;

import java.util.List;

@Data
public class OrderParamsDTO extends BaseParamsDTO {
    private static final long serialVersionUID = 1L;
    private String status;
    private Integer addressId;
    private String sysCnl;
    private String clientIp;
    private String orderNo;
    private List<Integer> cartIds;
    private Integer buyType;
    private String inviteCode;
    private String shipSn;
    private Long userBuyerId;
    private Long userId;
    private String receiveGiftMobile;
    private String areaCode;
}
