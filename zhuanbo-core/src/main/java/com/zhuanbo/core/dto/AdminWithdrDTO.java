package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提现DTO
 */
@Data
public class AdminWithdrDTO implements Serializable {

    private Long id;
    /**
     * 用户Id
     */
    private Integer userId;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 订单状态
     */
    private String orderStatus;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 页码
     */
    private Long page;
    /**
     * 页面大小
     */
    private Long limit;

    private List<String> orderNos;

    private List<Long> ids;

}
