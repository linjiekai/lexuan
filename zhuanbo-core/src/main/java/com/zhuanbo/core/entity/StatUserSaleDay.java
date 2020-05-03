package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

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
public class StatUserSaleDay implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 订单数量
     */
    private Integer vsOrderCount;

    /**
     * 订单金额
     */
    private BigDecimal vsOrderPrice;

    /**
     * 用户D 
     */
    private Long vsUserId;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatDate() {
		return statDate;
	}

	public void setStatDate(String statDate) {
		this.statDate = statDate;
	}

	public Integer getVsOrderCount() {
		return vsOrderCount;
	}

	public void setVsOrderCount(Integer vsOrderCount) {
		this.vsOrderCount = vsOrderCount;
	}

	public BigDecimal getVsOrderPrice() {
		return vsOrderPrice;
	}

	public void setVsOrderPrice(BigDecimal vsOrderPrice) {
		this.vsOrderPrice = vsOrderPrice;
	}

	public Long getVsUserId() {
		return vsUserId;
	}

	public void setVsUserId(Long vsUserId) {
		this.vsUserId = vsUserId;
	}

}
