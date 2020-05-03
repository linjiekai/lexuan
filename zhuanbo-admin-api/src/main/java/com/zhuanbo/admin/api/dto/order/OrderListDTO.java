package com.zhuanbo.admin.api.dto.order;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@ToString
public class OrderListDTO {

    private List<OrderGoods> orderGoods;

    private String orderNo;// 订单编号
    private Integer number;// 订单商品数量
    private BigDecimal price;// 订单金额
    private String orderStatus;// 订单状态
    private String payNo;// 支付流水
    private String payTime;// 支付时间
    private LocalDateTime deliveryTime;// 发货时间
    private String deliveryNo;// 发货单号
    private LocalDateTime addTime;// 创建时间
    private Integer goodsId;// 商品ID
    private Long userId;// 用户名
    private String adminName;// 操作人名称
    private String mobile;// 收货聊系人手机号
    private String username;// 收货聊系人名称
    private String province;// 收货聊系人省
    private String city;// 收货聊系人市
    private String area;// 收货聊系人区
    private String country;// 收货聊系人乡镇
    private String address;// 收货聊系人地址
    private List<String> supplierList;// 供应商信息
    private List<Integer> traceTypeList;// 贸易方式
    private List<Ships> shipList;
    private String remark;// 备注
    private Long invitePid;//邀请上级
    @Data
    @Builder
    @ToString
    public static class OrderGoods{
        private Integer goodsId;// 商品ID
        private String goodsName;// 订单商品名称
        private String[] specifications;// 规格信息
        private String traceType;   //贸易类型 0：一般贸易、1：保税/香港直邮、2：海外直邮（非韩澳）、3：海外直邮（韩澳）
        private String supplierName; //供应商。1：幸福狐狸
        private int dockingType; //
    }

    @Data
    @Builder
    @ToString
    public static class Ships{
        private String orderNo;
        private String shipChannel;
        private String shipSn;
    }
}
