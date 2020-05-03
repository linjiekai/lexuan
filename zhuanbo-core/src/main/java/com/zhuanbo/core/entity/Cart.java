package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 购物车商品表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_cart")
@Data
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 用户表的用户ID
     */
    private Long userId;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品类型 0：普通商品 1：会员商品
     */
    private Integer goodsType;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品货品表的货品ID
     */
    private Integer productId;

    /**
     * 商品货品的价格
     */
    private BigDecimal price;

    /**
     * 利润价
     */
    private BigDecimal profitPrice;

    /**
     * 分享价
     */
    private BigDecimal sharePrice;
    
    /**
     * 贸易类型。0：一般贸易、1：保税/香港直邮、2：海外直邮（非韩澳）、3：海外直邮（韩澳）
     */
    private Integer traceType;


    /**
     * 商品货品的数量
     */
    private Integer number;

    /**
     * 商品规格值列表，采用JSON数组格式
     */
    private List specifications;

    /**
     * 购物车中商品是否展示(区分立即购买和非立即购买)
     */
    private Boolean checked;

    /**
     * 商品图片或者商品货品图片
     */
    private String picUrl;

    private Integer categoryId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Boolean deleted;

    /**
     * 乐观锁字段
     */
    private Integer version;
}
