package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空白格模板
 */
@TableName("shop_blank_template")
@Data
public class BlankTemplate implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板中排序索引
     */
    private Long index;
    /**
     * 高度
     */
    private Long hight;
    /**
     * 父模板id
     */
    private Long templatePid;

    private LocalDateTime createTime;
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
