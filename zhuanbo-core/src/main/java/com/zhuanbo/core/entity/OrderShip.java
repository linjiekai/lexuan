package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单物流信息表
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
@TableName("shop_order_ship")
public class OrderShip implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单物流日期
     */
    private String orderDate;

    /**
     * 订单物流时间
     */
    private String orderTime;

    /**
     * 物流订单状态 已完成:S 已取消:C 待收货:WD
     */
    private String orderStatus;

    /**
     * 物流公司编号 
     */
    private String shipChannel;

    /**
     * 物流单号
     */
    private String shipSn;

    /**
     * 路由信息 json格式
     */
    private String routeInfo;

    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    public String getShipChannel() {
        return shipChannel;
    }

    public void setShipChannel(String shipChannel) {
        this.shipChannel = shipChannel;
    }
    public String getShipSn() {
        return shipSn;
    }

    public void setShipSn(String shipSn) {
        this.shipSn = shipSn;
    }
    public String getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(String routeInfo) {
        this.routeInfo = routeInfo;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "ShopOrderShip{" +
        "id=" + id +
        ", orderNo=" + orderNo +
        ", orderDate=" + orderDate +
        ", orderTime=" + orderTime +
        ", orderStatus=" + orderStatus +
        ", shipChannel=" + shipChannel +
        ", shipSn=" + shipSn +
        ", deleted=" + deleted +
        ", routeInfo=" + routeInfo +
        ", addTime=" + addTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
