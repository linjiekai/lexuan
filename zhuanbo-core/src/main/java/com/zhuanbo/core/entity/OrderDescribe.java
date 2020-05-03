package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单副表
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_order_describe")
@Data
public class OrderDescribe implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 联系人姓名
     */
    private String contactsName;

    /**
     * 联系人手机号
     */
    private String mobile;

    /**
     * 联系人电话
     */
    private String tel;

    /**
     * 行政区域表的省ID
     */
    private Integer provinceId;

    /**
     * 行政区域表的市ID
     */
    private Integer cityId;

    /**
     * 行政区域表的区县ID
     */
    private Integer areaId;

    /**
     * 行政区域表的乡镇ID
     */
    private Integer countryId;

    /**
     * 联系人地址
     */
    private String address;

    /**
     * 邮政编码
     */
    private String postcode;

    /**
     * 商品总额
     */
    private BigDecimal goodsTotalPrice;

    /**
     * 配送金额
     */
    private BigDecimal shipPrice;

    /**
     * 物流公司 
     */
    private String shipChannel;

    /**
     * 物流编号
     */
    private String shipSn;

    /**
     * 物流发货时间 YYYY-MM-DD HH:mm:ss
     */
    private String shipTime;

    /**
     * 用户确认收货时间 YYYY-MM-DD HH:mm:ss
     */
    private String confirmTime;

    /**
     * 优惠编号
     */
    private String couponSn;

    /**
     * 优惠金额
     */
    private BigDecimal couponPrice;
    
    /**
     * 订购人ID
     */
    private Long userBuyerId;

    /**
     * 操作人id
     */
    private Long adminId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 赠品接收人手机号
     */
    private String receiveGiftMobile;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
