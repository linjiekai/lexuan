package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单与条关联表
 * </p>
 *
 * @author rome
 * @since 2019-10-29
 */
@TableName("shop_order_refund")
@Data
public class OrderRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long userId;

    /**
     * 退款订单号
     */
    private String orderRefundNo;

    /**
     * 退款金额
     */
    private BigDecimal refundPrice;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付订单金额
     */
    private BigDecimal price;

    /**
     * 订单状态 等待退款W,退款成功S,退款失败F
     */
    private String orderStatus;

    /**
     * 退款订单日期
     */
    private String refundDate;

    /**
     * 退款订单时间
     */
    private String refundTime;

    /**
     * 返回码
     */
    private String payRespCode;

    /**
     * 返回描述
     */
    private String payRespMsg;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人ID
     */
    private Integer adminId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getOrderRefundNo() {
        return orderRefundNo;
    }

    public void setOrderRefundNo(String orderRefundNo) {
        this.orderRefundNo = orderRefundNo;
    }
    public BigDecimal getRefundPrice() {
        return refundPrice;
    }

    public void setRefundPrice(BigDecimal refundPrice) {
        this.refundPrice = refundPrice;
    }
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    public String getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(String refundDate) {
        this.refundDate = refundDate;
    }
    public String getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(String refundTime) {
        this.refundTime = refundTime;
    }
    public String getPayRespCode() {
        return payRespCode;
    }

    public void setPayRespCode(String payRespCode) {
        this.payRespCode = payRespCode;
    }
    public String getPayRespMsg() {
        return payRespMsg;
    }

    public void setPayRespMsg(String payRespMsg) {
        this.payRespMsg = payRespMsg;
    }
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ShopOrderRefund{" +
        "id=" + id +
        ", userId=" + userId +
        ", orderRefundNo=" + orderRefundNo +
        ", refundPrice=" + refundPrice +
        ", orderNo=" + orderNo +
        ", price=" + price +
        ", orderStatus=" + orderStatus +
        ", refundDate=" + refundDate +
        ", refundTime=" + refundTime +
        ", payRespCode=" + payRespCode +
        ", payRespMsg=" + payRespMsg +
        ", remark=" + remark +
        ", adminId=" + adminId +
        ", operator=" + operator +
        ", addTime=" + addTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
