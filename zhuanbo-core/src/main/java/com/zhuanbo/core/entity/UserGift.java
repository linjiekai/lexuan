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
 * 用户礼包表
 * </p>
 *
 * @author rome
 * @since 2019-11-22
 */
@TableName("shop_user_gift")
@Data
public class UserGift implements Serializable {

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
     * 基础礼包数量
     */
    private Integer baseNum;

    /**
     * 已使用基础礼包数量
     */
    private Integer baseNumUse;

    /**
     * 赠送礼包数量
     */
    private Integer giftNum;

    /**
     * 已使用赠送礼包数量
     */
    private Integer giftNumUse;
    
    /**
     * 已锁定赠送礼包数量
     */
    private Integer giftNumLock;

    /**
     * 收益状态 1：有效 2：已扣除 3：已过期 4：冻结中 5：冻结返还 6：冻结扣减
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
     * 
     */
    private Integer vipNum;

    /**
     * json 历史礼包数量
     */
    private String historyNum;

    /**
     * 内容
     */
    private String content;

    /**
     * 基础礼包线下数量
     */
    private Integer baseNumOffline;

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
