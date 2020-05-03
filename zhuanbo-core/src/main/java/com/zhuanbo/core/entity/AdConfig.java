package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 广告上线配置表
 * </p>
 *
 * @date 2019/11/1 17:07
 */
@TableName("shop_ad_config")
@Data
public class AdConfig implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 广告位 [1:启动页 2:首页 3:个人中心 4:优享广告 5:课时费 6:有猫腻]
     */
    private Integer position;

    /**
     * 广告上线数量
     */
    private Integer onlineCount;

    /**
     * 上线策略业务名称
     */
    private String strategyService;

    /**
     * 是否更新redis缓存标识 [0:不, 1:更新]
     */
    private Integer refreshRedisFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

}
