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

/**
 * <p>
 * 订单商品表
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_order_goods")
@Data
public class OrderGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品类型 0：普通商品 1：会员商品
     */
    private Integer goodsType;

    /**
     * 商品货品表的货品ID
     */
    private Integer productId;

    /**
     * 商品货品的购买数量
     */
    private Integer number;

    /**
     * 商品货品的售价
     */
    private BigDecimal price;

    /**
     * 分享价
     */
    private BigDecimal sharePrice;
    
    /**
     * 上级1销售奖励
     */
    private BigDecimal higherOnePrice;

    /**
     * 上级2销售奖励
     */
    private BigDecimal higherTwoPrice;
    
    /**
     * 贸易类型。0：一般贸易、1：保税/香港直邮、2：海外直邮（非韩澳）、3：海外直邮（韩澳）
     */
    private Integer traceType;
    
    /**
     * M体验官-直属团队销售奖励
     */
    private BigDecimal train;

    /**
     * M体验官-平级团队奖励
     */
    private BigDecimal trainEq;

    /**
     * M司令-直属团队销售奖励（达人）
     */
    private BigDecimal servIndt;

    /**
     * M司令-直属团队销售奖励（体验官）
     */
    private BigDecimal servLower;

    /**
     * M司令-平级团队奖励
     */
    private BigDecimal servEq;
    
    /**
     * M司令(合伙人)利润
     */
    private BigDecimal partner;

    /**
     * M司令(高级合伙人)利润
     */
    private BigDecimal highPartner;

    /**
     * M司令(总监)利润
     */
    private BigDecimal director;

    /**
     * M司令(高级总监)利润
     */
    private BigDecimal highDirector;

    /**
     * M司令(代言人)利润
     */
    private BigDecimal spokesman;

    /**
     * M司令(高级代言人)利润
     */
    private BigDecimal highSpokesman;
    
    /**
     * 
     */
    private Integer buyerPartner;

    /**
     * 商品货品的规格列表
     */
    private String[] specifications;

    /**
     * 商品货品图片或者商品图片
     */
    private String picUrl;

    private Integer categoryId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    /**
     * 逻辑删除
     */
    private Boolean deleted;

    /**
     * 乐观锁字段
     */
    private Integer version;
}
