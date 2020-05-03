package com.zhuanbo.service.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户销量统计天报表
 * </p>
 *
 * @author rome
 * @since 2019-06-27
 */
@Data
public class StatUserTeamVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 订单数量
     */
    private Integer count;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 用户id
     */
    private Long userId;
    private String headImgUrl;
	private String nickname;
	private Integer ptLevel;
	private String ptNo;
	private String under;
	private String regDate;
	private String mobile;
}
