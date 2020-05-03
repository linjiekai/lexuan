package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("shop_index_template")
@Data
public class IndexTemplate implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 导航模板id列表
     */
    private String[] appBarTemplateIds;
    /**
     * 广告模板id列表
     */
    private String[] adTemplateIds;
    /**
     * 商品列表模板id列表
     */
    private String[] goodsListTemplateIds;
    /**
     * 秒杀模板id列表
     */
    private String[] flashSaleTemplateIds;
    /**
     * 空白格模板id列表
     */
    private String[] blankTemplateIds;
    /**
     * 辅助线模板id列表
     */
    private String[] guidesTemplateIds;
    /**
     * 状态,0:失效，1：生效
     */
    private Integer status;
    /**
     * 操作人id
     */
    private Long adminId;

    private LocalDateTime createTime;
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

}
