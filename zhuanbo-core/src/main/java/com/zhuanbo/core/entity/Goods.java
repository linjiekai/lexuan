package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品基本信息表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_goods")
@Data
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品副名称
     */
    private String sideName;

    /**
     * 商品页面商品图片或者视频
     */
    private String videoUrl;

    private String videoId;

    private String videoTranscodeUrl;

    /**
     * 商品宣传图片列表，采用JSON数组格式
     */
    private String[] coverImages;

    private String[] detail;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 普通用户价格
     */
    private BigDecimal plain;

    /**
     * VIP价格
     */
    private BigDecimal plus;

    /**
     * 县级店价格
     */
    private BigDecimal train;

    /**
     * 品牌店价格
     */
    private BigDecimal serv;
    
    /**
     * 金钻价格
     */
    private BigDecimal partner;

    /**
     * 总裁价格
     */
    private BigDecimal director;

    /**
     * 分公司价格
     */
    private BigDecimal spokesman;

    /**
     * 商品状态： 0：下架  1：上架 2:缺货
     */
    private Integer status;

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

    private Integer goodsType;

    /**
     * 后台管理员id
     */
    private Integer adminUserId;
}
