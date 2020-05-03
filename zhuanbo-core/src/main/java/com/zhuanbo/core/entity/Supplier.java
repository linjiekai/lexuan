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
 * 供应商表
 * </p>
 *
 * @author rome
 * @since 2019-08-12
 */
@TableName("shop_supplier")
@Data
public class Supplier implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     *  供应商编码 4位数字 0001 0002···
     */
    private String code;

    /**
     * 订单对接方式。0：自动、1：手动
     */
    private Integer orderConnectType;

    /**
     * 操作人id
     */
    private Long operatorId;

    /**
     * 操作人
     */
    private String operator;

    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
