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
 * 用户礼包明细表
 * </p>
 *
 * @author rome
 * @since 2019-11-22
 */
@TableName("shop_user_gift_details")
@Data
public class UserGiftDetails implements Serializable {

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
     * 用户等级0:普通用户;1:VIP;2:县级店;3:品牌店;4:金钻;5:总裁;6:分公司
     */
    private Integer ptLevel;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 进货类型 1：购买自用 2：进货云仓 3：提货 4：线下发货
     */
    private Integer purchType;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long fromUserId;

    /**
     * 用户等级0:普通用户;1:VIP;2:县级店;3:品牌店;4:金钻;5:总裁;6:分公司
     */
    private Integer fromPtLevel;
    
    /**
     * 指标类型 [1:基础礼包, 2:赠送礼包]
     */
    private Integer changeType;

    /**
     * 操作收益 本次操作的收益值
     */
    private Integer operateGift;

    /**
     * 操作类型 [1:增加收益, 2:减少收益]
     */
    private Integer operateType;

    /**
     * 收益状态 1：有效 2：已提取 3：已过期 4：冻结中 5：冻结返还 6：冻结扣减
     */
    private Integer status;

    /**
     * 登记日期yyyy-MM-dd
     */
    private String giftDate;

    /**
     * 登记时间HH:mm:ss
     */
    private String giftTime;

    /**
     * 礼包类型 [0:普通 1:基础, 2:赠送 3:礼包]
     */
    private Integer giftType;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;
    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
    
}
