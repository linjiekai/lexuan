package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.PtLevelType;
import com.zhuanbo.core.entity.StatIncomeDay;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.ReportExcel;
import com.zhuanbo.service.mapper.StatIncomeDayMapper;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IStatIncomeDayService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.vo.StatDepositOrderGroupByVo;
import com.zhuanbo.service.vo.StatIncomeDayVO;
import com.zhuanbo.service.vo.StatUserCountVO;
import com.zhuanbo.service.vo.UserIncomeDetailsStatVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 收益日统计报表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-20
 */
@Service
@Slf4j
public class StatIncomeDayServiceImpl extends ServiceImpl<StatIncomeDayMapper, StatIncomeDay> implements IStatIncomeDayService {

	@Autowired
    private IOrderService orderService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserIncomeDetailsService userIncomeDetailsService;

    @Autowired
    private IUserInviteService userInviteService;
    @Autowired
    private IDepositOrderService iDepositOrderService;

    @Override
    public void statIncomeDay(String batchDate) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("statDate", batchDate);
        StatIncomeDay statIncomeDayTemp = orderService.statUserSale(params);

        int vsTotalUserCount = userService.count(new QueryWrapper<User>().eq("status", ConstantsEnum.USER_STATUS_1.integerValue()));
        int vsUserCount = userService.count(new QueryWrapper<User>().eq("reg_date", batchDate).eq("status", ConstantsEnum.USER_STATUS_1.integerValue()));
        int vsUserInviteCount = userInviteService.count(new QueryWrapper<UserInvite>().eq("DATE_FORMAT(add_time,'%Y-%m-%d')", batchDate));

        params = new HashMap<>();
        params.put("incomeDate", batchDate);

        // 商品销售奖励
        BigDecimal vsIncomeTypeOne = new BigDecimal(0);
        // 服务商销售奖励
        BigDecimal vsIncomeTypeTwo = new BigDecimal(0);
        // 下级销售扣减
        BigDecimal vsIncomeTypeThree = new BigDecimal(0);
        // 进货差价奖励
        BigDecimal vsIncomeTypeFive = new BigDecimal(0);
        // 下级运费扣减
        BigDecimal vsIncomeTypeSix = new BigDecimal(0);
        // 累计收益
        BigDecimal vsTotalPrice = new BigDecimal(0);
        // 提现金额
        BigDecimal vsWithdrPrice = new BigDecimal(0);

        List<UserIncomeDetailsStatVO> incomeStatList = userIncomeDetailsService.incomeStatGruopBy(params);

        if (null != incomeStatList) {
            for (UserIncomeDetailsStatVO vo : incomeStatList) {
                switch (vo.getIncomeType()) {
                    case 1:
                        vsIncomeTypeOne = vsIncomeTypeOne.add(vo.getOperateIncome());
                        vsTotalPrice = vsTotalPrice.add(vo.getOperateIncome());
                        break;
                    case 2:
                        vsIncomeTypeTwo = vsIncomeTypeTwo.add(vo.getOperateIncome());
                        vsTotalPrice = vsTotalPrice.add(vo.getOperateIncome());
                        break;
                    case 3:
                        vsIncomeTypeThree = vsIncomeTypeThree.add(vo.getOperateIncome());
                        vsTotalPrice = vsTotalPrice.add(vo.getOperateIncome());
                        break;
                    case 4:
                        vsWithdrPrice = vsWithdrPrice.add(vo.getOperateIncome());
                        break;
                    case 5:
                        vsIncomeTypeFive = vsIncomeTypeFive.add(vo.getOperateIncome());
                        vsTotalPrice = vsTotalPrice.add(vo.getOperateIncome());
                        break;
                    case 6:
                        vsIncomeTypeSix = vsIncomeTypeSix.add(vo.getOperateIncome());
                        vsTotalPrice = vsTotalPrice.add(vo.getOperateIncome());
                        break;
                    default:
                        break;
                }
            }
        }

        StatIncomeDay statIncomeDay = new StatIncomeDay();

        StatIncomeDay statIncomeDayOld = this.getOne(new QueryWrapper<StatIncomeDay>().eq("stat_date", batchDate));
        if (null != statIncomeDayOld) {
            statIncomeDay.setId(statIncomeDayOld.getId());
        }

        statIncomeDay.setStatDate(batchDate);
        statIncomeDay.setVsOrderCount(statIncomeDayTemp.getVsOrderCount());
        statIncomeDay.setVsOrderPrice(statIncomeDayTemp.getVsOrderPrice());
        statIncomeDay.setVsWithdrIncome(vsWithdrPrice);
        statIncomeDay.setVsTotalUserCount(vsTotalUserCount);
        statIncomeDay.setVsIncomeTypeOne(vsIncomeTypeOne);
        statIncomeDay.setVsIncomeTypeTwo(vsIncomeTypeTwo);
        statIncomeDay.setVsIncomeTypeThree(vsIncomeTypeThree);
        statIncomeDay.setVsIncomeTypeFive(vsIncomeTypeFive);
        statIncomeDay.setVsIncomeTypeSix(vsIncomeTypeSix);
        statIncomeDay.setVsTotalIncome(vsTotalPrice);
        statIncomeDay.setVsUserCount(vsUserCount);
        statIncomeDay.setVsUserInviteCount(vsUserInviteCount);


        params = new HashMap<String, Object>();
        params.put("payDate", batchDate);
        params.put("busiType", "06");
        params.put("orderStatus", "S");
        List<StatDepositOrderGroupByVo> statDepositOrderGroupByVoList = iDepositOrderService.statDepositOrderGroupBy(params);
        if(CollectionUtils.isNotEmpty(statDepositOrderGroupByVoList)){
            for(StatDepositOrderGroupByVo vo : statDepositOrderGroupByVoList){
                Integer orderType = vo.getOrderType();
                switch (orderType){
                    case 1:
                        statIncomeDay.setVsDepositPriceOne(vo.getVsDepositPrice());
                        statIncomeDay.setVsDepositCountOne(vo.getVsDepositCount());
                        break;
                    case 2:
                        statIncomeDay.setVsDepositPriceTwo(vo.getVsDepositPrice());
                        statIncomeDay.setVsDepositCountTwo(vo.getVsDepositCount());
                        break;
                    case 3:
                        statIncomeDay.setVsDepositPriceThree(vo.getVsDepositPrice());
                        statIncomeDay.setVsDepositCountThree(vo.getVsDepositCount());
                        break;
                    case 4:
                        statIncomeDay.setVsDepositPriceFour(vo.getVsDepositPrice());
                        statIncomeDay.setVsDepositCountFour(vo.getVsDepositCount());
                        break;
                    case 5:
                        statIncomeDay.setVsDepositPriceFive(vo.getVsDepositPrice());
                        statIncomeDay.setVsDepositCountFive(vo.getVsDepositCount());
                        break;
                }
            }
        }

        this.saveOrUpdate(statIncomeDay);
    }

    @Override
    public void exportData(String startDate, String endDate, HttpServletRequest request, HttpServletResponse response) throws Exception {
        QueryWrapper<StatIncomeDay> statIncomeDayQueryWrapper = new QueryWrapper<>();
        statIncomeDayQueryWrapper.orderByAsc("stat_date");
        if (StringUtils.isEmpty(startDate)) {
            startDate = DateUtil.dateFormat(DateUtil.dateAddMonths(new Date(), -3), DateUtil.DATE_PATTERN);
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = DateUtil.dateFormat(new Date(), DateUtil.DATE_PATTERN);
        }
        statIncomeDayQueryWrapper.ge("stat_date", startDate);
        statIncomeDayQueryWrapper.le("stat_date", endDate);
        statIncomeDayQueryWrapper.orderByDesc("id");
        int count = count(statIncomeDayQueryWrapper);
        statIncomeDayQueryWrapper.last("limit 0," + count);
        List<StatIncomeDay> list = list(statIncomeDayQueryWrapper);
        List<StatIncomeDayVO> vos = Lists.newArrayList();
        StatIncomeDayVO vo;
        for (StatIncomeDay item : list) {
            vo = new StatIncomeDayVO();
            BeanUtils.copyProperties(item, vo);
            vos.add(vo);
        }
        ReportExcel reportExcel = new ReportExcel();
        reportExcel.excelExport(vos, "日报表 " + DateUtil.toyyyy_MM_dd(LocalDateTime.now()), StatIncomeDayVO.class, 1, response, request);

    }

    @Override
    public List<Map<String,Object>> statUserCountRadius(){
        List<Map<String,Object>> resultList = new ArrayList<>();
        Map<String,Object> params = new HashMap<>();
        params.put("endDate", DateUtil.toyyyy_MM_dd_HH_mm_ss(LocalDateTime.now()));
        params.put("ptLevel", PtLevelType.levelList());
        List<Map<String,Object>> list = baseMapper.statUserCount(params);
        for(Map<String,Object> obj: list){
            Map<String,Object> ent = new HashMap<>();
            ent.put("name", PtLevelType.toName(Integer.parseInt(obj.get("level").toString())));
            ent.put("value", obj.get("count"));
            resultList.add(ent);
        }
        return resultList;
    }

    @Override
    public List<Map<String,Object>> statUserCountAxis(String startDateStr, String endDateStr, List<Integer> ptLevelList) throws ParseException {
        log.info("用户分布轴图数据统计，startDateStr={},endDateStr={},ptLevelList={}",startDateStr,endDateStr,ptLevelList.toArray());
        List<Map<String,Object>> resultList = new ArrayList<>();
        startDateStr = startDateStr + " 00:00:00";
        endDateStr = endDateStr + " 23:59:59";
        Map<String,Object> params = new HashMap<>();
        params.put("startDate",startDateStr);
        params.put("endDate",endDateStr);
        params.put("ptLevel",ptLevelList);
        List<Map<String,Object>> list = baseMapper.statUserLevelByDate(params);
        params.put("ptLevel",new int[]{0});
        list.addAll(baseMapper.statUserCountByDate(params));

        Date startDate = DateUtil.dateParse(startDateStr,DateUtil.DATE_PATTERN);
        Date endDate = DateUtil.dateParse(endDateStr,DateUtil.DATE_PATTERN);
        for(Integer ptLevel: ptLevelList){
            Map<String,Object> ent = new HashMap<>();
            ent.put("name", PtLevelType.toName(ptLevel));
            List<Integer> countList = new ArrayList<>();
            Date statDate = startDate;
            while(DateUtil.dateCompare(endDate,statDate) != -1){
                String stat = DateUtil.dateFormat(statDate,DateUtil.DATE_PATTERN);
                Integer count = 0;
                //匹配结果
                for(Map<String,Object> obj : list){
                    if(ptLevel.equals(obj.get("level")) && stat.equals(obj.get("date"))){
                        count = Integer.parseInt(obj.get("count").toString());
                    }
                }
                countList.add(count);
                statDate = DateUtil.dateAdd(statDate,+1,false);
            }
            ent.put("value", countList);
            resultList.add(ent);
        }
        return resultList;
    }


    @Override
    public List<StatUserCountVO> statUserCount(String startDateStr, String endDateStr) throws ParseException {
        log.info("用户分布数据统计，startDateStr={},endDateStr={}",startDateStr,endDateStr);
        List<StatUserCountVO> resultList = new ArrayList<>();
        startDateStr = startDateStr + " 00:00:00";
        endDateStr = endDateStr + " 23:59:59";
        Map<String,Object> params = new HashMap<>();
        params.put("startDate",startDateStr);
        params.put("endDate",endDateStr);
        params.put("ptLevel",PtLevelType.levelList());
        List<Map<String,Object>> list = baseMapper.statUserLevelByDate(params);
        params.put("ptLevel",new int[]{0});
        list.addAll(baseMapper.statUserCountByDate(params));

        Date startDate = DateUtil.dateParse(startDateStr,DateUtil.DATE_PATTERN);
        Date endDate = DateUtil.dateParse(endDateStr,DateUtil.DATE_PATTERN);
        Date statDate = startDate;
        while(DateUtil.dateCompare(endDate,statDate) != -1){
            String stat = DateUtil.dateFormat(statDate,DateUtil.DATE_PATTERN);
            StatUserCountVO ent = new StatUserCountVO();
            ent.setStatDate(stat);

            for(PtLevelType ptLevel: PtLevelType.values()){
                Integer count = 0;
                //匹配结果
                for(Map<String,Object> obj : list){
                    if((ptLevel.getId() == Integer.parseInt(obj.get("level").toString())) && stat.equals(obj.get("date"))){
                        count = Integer.parseInt(obj.get("count").toString());
                    }
                }
                switch (ptLevel.getId()){
                    case 0:
                        ent.setPtLevelCount0(count);
                        break;
                    case 1:
                        ent.setPtLevelCount1(count);
                        break;
                    case 2:
                        ent.setPtLevelCount2(count);
                        break;
                    case 3:
                        ent.setPtLevelCount3(count);
                        break;
                    case 4:
                        ent.setPtLevelCount4(count);
                        break;
                    case 5:
                        ent.setPtLevelCount5(count);
                        break;
                    default:
                        break;
                }
            }
            resultList.add(ent);
            statDate = DateUtil.dateAdd(statDate,+1,false);
        }

        return resultList;
    }

    @Override
    public List<String> dateBreakUp(String startDateStr, String endDateStr) throws ParseException {
        String pattern = "MM-dd";
        List<String> resultList = new ArrayList<>();
        Date startDate = DateUtil.dateParse(startDateStr,DateUtil.DATE_PATTERN);
        Date endDate = DateUtil.dateParse(endDateStr,DateUtil.DATE_PATTERN);
        while(DateUtil.dateCompare(endDate,startDate) != -1){
            String stat = DateUtil.dateFormat(startDate,pattern);
            resultList.add(stat);
            startDate = DateUtil.dateAdd(startDate,+1,false);
        }
        return resultList;
    }

}
