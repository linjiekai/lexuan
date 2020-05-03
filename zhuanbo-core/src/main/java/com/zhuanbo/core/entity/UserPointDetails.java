package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户积分明细表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_user_point_details")
@Data
public class UserPointDetails implements Serializable {

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
     * 订单号
     */
    private String orderNo;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 操作积分 本次操作的积分值
     */
    private Integer operatePoint;

    /**
     * 可用积分
     */
    private Integer usablePoint;

    /**
     * 操作类型 1：增加积分 2：减少积分
     */
    private Integer operateType;

    /**
     * 业务类型 1：推荐购物(下级消费送) 2：支付扣减(兑换商品) 9：过期等
     */
    private Integer pointType;

    /**
     * 积分状态 1：有效 2：已扣除(扣完) 3：已过期 4：冻结中 5：冻结返还 6：冻结扣减
     */
    private Integer status;

    /**
     * 登记日期yyyy-MM-dd
     */
    private String pointDate;

    /**
     * 登记时间HH:mm:ss
     */
    private String pointTime;

    /**
     * 唯一标识一个用户,user表ID关联
     */
    private Long fromUserId;

    /**
     * 积分来源  当扣减积分时，存储扣减的积分记录id，用于追溯积分 json格式
     */
    private String pid;

    /**
     * 过期时间 YYYYMMDDHHMMSS
     */
    private String expTime;

    /**
     * 内容
     */
    private String content;

    /**
     * 操作人id
     */
    private Long operatorId;

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
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    private String remark;
}
