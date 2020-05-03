package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author rome
 * @since 2019-06-20
 */
@TableName("shop_operate_log")
@Data
public class OperateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作模块 商品列表、收益记录、课时费设置、收支报表、日报表
     */
    private String operateType;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作人ID
     */
    private Long operateId;

    /**
     * 操作日期
     */
    private String operateDate;

    /**
     * 操作时间
     */
    private String operateTime;

    /**
     * 操作内容 新建、导出、编辑、删除
     */
    private String operateAction;

    /**
     * 操作目标id
     */
    private String targetId;

    /**
     * 客户端IP
     */
    private String clientIp;
    
    private LocalDateTime addTime;
}
