package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播意向申请
 */
@Data
public class LiveRoomApplyDTO extends BaseDTO {

    private Long id;

    private Long userId;

    /**
     * 直播用户userId
     */
    private Long liveUserId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 直播标题
     */
    private String title;

    /**
     * 第三方创建开通直播昵称
     */
    private String openAnchorNickname;

    /**
     * 开播：开始时间
     */
    private LocalDateTime startTime;

    /**
     * 开播：结束时间
     */
    private LocalDateTime endTime;

    /**
     * 主播昵称
     */
    private String anchorNickname;

    /**
     * 主播微信号
     */
    private String wxNo;

    /**
     * 状态 0:待审核 1:通过 2:拒绝 3:结束
     */
    private Integer status;

    /**
     * 是否允许评论 0:不允许 1:允许
     */
    private Integer commentFlag;

    /**
     * 分享卡片封面
     */
    private String shareImg;

    /**
     * 直播间封面
     */
    private String coverImg;

    /**
     * 关联商品名称
     */
    private String goodsName;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
