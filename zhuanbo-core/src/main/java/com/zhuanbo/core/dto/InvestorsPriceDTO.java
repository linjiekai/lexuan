package com.zhuanbo.core.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 商品获取dto
 */
@Data
public class InvestorsPriceDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String name;
	private Integer goodsType;
	//价格
	private BigDecimal price;
	//普通用户价格
	private BigDecimal plain;
	//vip价格
	private BigDecimal plus;
	//店长价格
	private BigDecimal train;
	//总监价格
	private BigDecimal serv;
	//合伙人价格
	private BigDecimal partner;
	//联创价格
	private BigDecimal director;
	private BigDecimal spokesman;
	private Integer status;
	private LocalDateTime updateTime;
	private String adminUserName;

	public String getLevelAndPrice() {
		StringBuffer sbuff = new StringBuffer();
//		sbuff.append("1," + plus + ";");
		sbuff.append("2," + train + ";");
		sbuff.append("3," + serv + ";");
		sbuff.append("4," + partner + ";");
//		sbuff.append("5," + director + ";");
		return sbuff.toString();
	}
	
	public BigDecimal getLevelPrice(Integer ptLeve) {
		BigDecimal price = new BigDecimal("0");
		switch (ptLeve) {
		case 0:
			price = this.plain;
			break;
		case 1:
			price = this.plus;
			break;
		case 2:
			price = this.train;
			break;
		case 3:
			price = this.serv;
			break;
		case 4:
			price = this.partner;
			break;
		case 5:
			price = this.director;
			break;
		default:
			price = this.price;
			break;
		}
		
		return price;
	}
}
