package com.zhuanbo.service.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.service.mapper.UserIncomeDetailsMapper;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.vo.MarginVO;
import com.zhuanbo.service.vo.UserIncomeDetailsStatVO;
import com.zhuanbo.service.vo.UserIncomeDetailsVO;

/**
 * <p>
 * 用户收益明细表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
public class UserIncomeDetailsServiceImpl extends ServiceImpl<UserIncomeDetailsMapper, UserIncomeDetails>
		implements IUserIncomeDetailsService {

	@Override
    public Page<UserIncomeDetailsVO> listMap(Page<UserIncomeDetailsVO> page, Map<String, Object> params) {
        List<UserIncomeDetailsVO> userIncomeDetailsVOS = baseMapper.listMap(page, params);
        if (page == null) {
            page = new Page<>();
            page.setTotal(userIncomeDetailsVOS.size());
        }
        if (page.getSize() == -1) {
            page.setTotal(userIncomeDetailsVOS.size());
        }

        if (null == userIncomeDetailsVOS) {
            userIncomeDetailsVOS = new ArrayList<UserIncomeDetailsVO>();
        }

        page.setRecords(userIncomeDetailsVOS);
        return page;
    }

    @Override
    public BigDecimal totalIncome(Map<String, Object> params) {
        return baseMapper.totalIncome(params);
    }

    @Override
    public UserIncomeDetailsStatVO incomeStat(Map<String, Object> params) {
        return baseMapper.incomeStat(params);
    }

    @Override
    public List<UserIncomeDetailsStatVO> incomeStatGruopBy(Map<String, Object> params) {
        return baseMapper.incomeStatGruopBy(params);
    }

    @Override
    public Page<MarginVO> marginChangeList(Page<MarginVO> page, Long userId, String userName, String authNo,
                                           String startDate, String endDate) throws Exception {
        Map<String,Object> params = new HashMap<String, Object>();
        if(null != userId){
            params.put("userId", userId);
        }
        if(StringUtils.isNotBlank(userName)){
            params.put("userName", userName);
        }
        if(StringUtils.isNotBlank(authNo)){
            params.put("authNo", authNo);
        }
        if(StringUtils.isNotBlank(startDate)){
            params.put("startDate", startDate);
        }
        if(StringUtils.isNotBlank(endDate)){
            params.put("endDate", endDate);
        }
        List<MarginVO> marginVOS = baseMapper.listMargin(page, params);
        if (null != marginVOS && marginVOS.size() > 0) {
            for (MarginVO margin : marginVOS) {
                Integer operateType = margin.getOperateType();
                String price = margin.getPrice();
                if (1 == operateType.intValue()) {
                    margin.setPrice("+" + price);
                } else if (2 == operateType.intValue()) {
                    margin.setPrice("-" + price);
                }
            }
        }

        page.setRecords(marginVOS);
        return page;
    }

	@Override
	public void incomde2Deposit(Order order) {
		
	}

	@Override
	public BigDecimal marginTotal(Long userId){
        BigDecimal result = new BigDecimal(0);
        if(null != userId){
            List<UserIncomeDetails> userIncomeDetailsList = list(new QueryWrapper<UserIncomeDetails>().eq("income_type",8).eq("user_id",userId));
            if(CollectionUtils.isNotEmpty(userIncomeDetailsList)){
                for(UserIncomeDetails details : userIncomeDetailsList){
                    Integer operateType = details.getOperateType();
                    BigDecimal price = details.getPrice();
                    if(1 == operateType){
                        result = result.add(price);
                    }else if(2 == operateType){
                        result = result.subtract(price);
                    }

                }
            }
        }
        return result;
    }
	
}
