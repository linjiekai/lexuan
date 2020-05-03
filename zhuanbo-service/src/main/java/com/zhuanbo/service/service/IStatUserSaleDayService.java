package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.StatUserSaleDay;
import com.zhuanbo.service.vo.StatUserTeamVO;

import java.util.Map;

/**
 * <p>
 * 用户销量统计天报表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-27
 */
public interface IStatUserSaleDayService extends IService<StatUserSaleDay> {

	/**
	 * 用户销量按天保存
	 * @param statDate
	 */
	void saveUserSale(String statDate);
	
	/**
	 * 用户销量统计
	 * @param page
	 * @param params
	 * @return
	 */
	IPage<StatUserTeamVO> statUserSale(Page<StatUserTeamVO> page, Map<String, Object> params);

	/**
	 * 总销量统计
	 * @param page
	 * @param params
	 * @return
	 */
	public StatUserTeamVO statUserSaleTotal(Map<String, Object> params);
	
	/**
	 * 用消费统计
	 * @param params
	 * @return
	 */
	public StatUserTeamVO statUserConsumeTotal(Map<String, Object> params);
}
