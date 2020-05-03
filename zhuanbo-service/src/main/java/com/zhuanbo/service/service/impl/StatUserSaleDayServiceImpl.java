package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.StatUserSaleDay;
import com.zhuanbo.service.mapper.StatUserSaleDayMapper;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IStatUserSaleDayService;
import com.zhuanbo.service.vo.StatUserTeamVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户销量统计天报表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-27
 */
@Service
public class StatUserSaleDayServiceImpl extends ServiceImpl<StatUserSaleDayMapper, StatUserSaleDay> implements IStatUserSaleDayService {

	@Autowired
	private IOrderService iOrderService;
	
	@Autowired
	private IStatUserSaleDayService iStatUserSaleDayService;
	
	@Override
	public void saveUserSale(String statDate) {
		
		List<StatUserSaleDay> list = iOrderService.listUserSale(statDate);
		
		iStatUserSaleDayService.remove(new QueryWrapper<StatUserSaleDay>().eq("stat_date", statDate));
		
		if (null != list) {
			
			for (StatUserSaleDay statUserSaleDay : list) {
				statUserSaleDay.setStatDate(statDate);
				iStatUserSaleDayService.save(statUserSaleDay);
			}
		}
		
	}
	
	@Override
	public IPage<StatUserTeamVO> statUserSale(Page<StatUserTeamVO> page, Map<String, Object> params) {
		List<StatUserTeamVO> list = baseMapper.statUserSale(page, params);
		if (page == null) {
			page = new Page<>();
			page.setTotal(list.size());
		}
		if (page.getSize() == -1) {
			page.setTotal(list.size());
		}
		
		if (null == list) {
			list = new ArrayList<>();
		}
		
		page.setRecords(list);
		return page;
	}

	@Override
	public StatUserTeamVO statUserSaleTotal(Map<String, Object> params) {
		
		StatUserTeamVO vo = baseMapper.statUserSaleTotal(params);
		
		if (null == vo) {
			return new StatUserTeamVO();
		}
		
		return vo;
	}

	@Override
	public StatUserTeamVO statUserConsumeTotal(Map<String, Object> params) {
		StatUserTeamVO vo = baseMapper.statUserConsumeTotal(params);
		
		if (null == vo) {
			return new StatUserTeamVO();
		}
		
		return vo;
	}

	
}
