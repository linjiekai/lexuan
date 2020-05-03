package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 序列号表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_seq_incr")
@Data
public class SeqIncr implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 序列名称
     */
    private String name;

    /**
     * 当前序列值
     */
    private Long currentValue;
    
    /**
     * 每次增长值
     */
    private Integer increment;

    /**
     * 备注
     */
    private String remark;
    
    /**
     * 下一个增长的值
     */
    @TableField(exist = false)
    private Long nextValue;
}
