package com.zhuanbo.admin.api.controller;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.math.MathUtil;
import com.zhuanbo.core.annotation.LoginDealersAdmin;
import com.zhuanbo.core.constants.PtLevelType;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.entity.StatIncomeDay;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.ReportExcel;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IStatIncomeDayService;
import com.zhuanbo.service.vo.StatUserCountVO;

import lombok.extern.slf4j.Slf4j;

/**
 *日报表
 *
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/stat")
@Slf4j
public class StatIncomeDayController {

    @Autowired
    private IStatIncomeDayService iStatIncomeDayService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IUserInviteService iUserInviteService;


    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate) {

        Page<StatIncomeDay> statIncomeDayPage = new Page<>(page, limit);
        QueryWrapper<StatIncomeDay> statIncomeDayQueryWrapper = new QueryWrapper<>();
        statIncomeDayQueryWrapper.orderByDesc("stat_date");
        if (StringUtils.isNotBlank(startDate)) {
            statIncomeDayQueryWrapper.ge("stat_date", startDate);
        }
        if (StringUtils.isNotBlank(endDate)) {
            statIncomeDayQueryWrapper.le("stat_date", endDate);
        }

        IPage<StatIncomeDay> statIncomeDayIPage = iStatIncomeDayService.page(statIncomeDayPage, statIncomeDayQueryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", statIncomeDayIPage.getTotal());
        data.put("items", statIncomeDayIPage.getRecords());

        List<StatIncomeDay> list = iStatIncomeDayService.list(statIncomeDayQueryWrapper);
        BigDecimal allIncome = new BigDecimal(0);
        BigDecimal allWithdraw = new BigDecimal(0);
        for (StatIncomeDay statIncomeDay : list) {
            allIncome = allIncome.add(statIncomeDay.getVsTotalIncome());
            allWithdraw = allWithdraw.add(statIncomeDay.getVsWithdrIncome());
        }

        // 用户总收入[包含了用户各种收益类型]
        data.put("allIncome", allIncome);
        // 平台总支出[用户总提现金额]
        data.put("allWithdraw", allWithdraw);
        return ResponseUtil.ok(data);
    }

    @GetMapping("/exportData")
    public void exportData(@LoginAdmin Integer adminId,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        iStatIncomeDayService.exportData(startDate, endDate, request, response);
    }

    /**
     * 用户分布统计
     * @param adminId
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/statUserCount")
    public Object statUserCount(@LoginAdmin Integer adminId, @RequestBody JSONObject params) throws Exception {
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");
        List<Integer> ptLevelList = params.getJSONArray("ptLevels").toJavaList(Integer.class);

        log.info("用户分布数据统计，startDate={},endDate={},ptLevelList={}",startDate,endDate,ptLevelList.toArray());
        if(StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate) || (null == ptLevelList) || CollectionUtils.isEmpty(ptLevelList)){
            return ResponseUtil.badArgumentValue();
        }

        List<String> dateList = iStatIncomeDayService.dateBreakUp(startDate,endDate);
        List<Map<String,Object>> statUserCountRadius = iStatIncomeDayService.statUserCountRadius();
        List<Map<String,Object>> statUserCountAxis = iStatIncomeDayService.statUserCountAxis(startDate,endDate,ptLevelList);
        Map<String,Object> data = new HashMap<>();
        data.put("dateArray",dateList);
        data.put("radius",statUserCountRadius);
        data.put("axis",statUserCountAxis);
        return ResponseUtil.ok(data);
    }


    /**
     * 用户分布导出
     * @param adminId
     * @param params
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping("/exportUserCount")
    public void exportUserCount(@LoginAdmin Integer adminId, @RequestBody JSONObject params,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");

        log.info("用户分布数据统计导出，startDate={},endDate={}",startDate,endDate);
        if(StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)){
            throw new ShopException("传参异常");
        }

        List<StatUserCountVO> reusultList = iStatIncomeDayService.statUserCount(startDate,endDate);
        ReportExcel reportExcel = new ReportExcel();
        reportExcel.excelExport(reusultList, "userStat_" + DateUtil.toyyyy_MM_dd(LocalDateTime.now()), StatUserCountVO.class, 1, response, request);
    }


    @GetMapping("/user/summary")
    public Object userSummary(@LoginDealersAdmin Integer adminId) throws Exception {
        Map<String,Object> data = new HashMap<>();
        //用户总数（排除公司账号）
        int total = iUserService.count(new QueryWrapper<User>().ne("pt_level",PtLevelType.CC.getId()));
        data.put("total",total);
        //已处理邀请关系数：邀请上级为非1的用户数
        int inviteDealed = iUserInviteService.count(new QueryWrapper<UserInvite>().ne("pid",1));
        data.put("invite",inviteDealed);
        //邀请关系处理进度：邀请上级为非1的用户数/（总用户数-1）
        double inviteProgress = new BigDecimal(inviteDealed*100).divide(new BigDecimal(total-1),2, RoundingMode.HALF_UP).doubleValue();
        data.put("progress",inviteProgress);
        //代理级别分布
        List<Map<String,Object>> countGroupByLevel = iUserService.listMaps(new QueryWrapper<User>().select("pt_level","count(id) as value").ne("pt_level",PtLevelType.CC.getId()).groupBy("pt_level"));
        countGroupByLevel.stream().forEach(map -> {
            Integer level = (Integer) map.get("pt_level");
            map.put("name", PtLevelType.toName(level));
            map.remove("pt_level");
        });
        data.put("array",countGroupByLevel);

        return ResponseUtil.ok(data);
    }
}
