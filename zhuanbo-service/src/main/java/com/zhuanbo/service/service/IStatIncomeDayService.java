package com.zhuanbo.service.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.StatIncomeDay;
import com.zhuanbo.service.vo.StatUserCountVO;

/**
 * <p>
 * 收益日统计报表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-20
 */
public interface IStatIncomeDayService extends IService<StatIncomeDay> {

	/**
     * 收益日统计报表
     *
     * @param batchDate
     */
    void statIncomeDay(String batchDate) throws Exception;

    /**
     * @Description(描述):    导出数据
     * @auther: Jack Lin
     * @param :[startDate, endDate, request, response]
     * @return :void
     * @date: 2019/9/2 21:21
     */
    void exportData(String startDate,String endDate, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 用户分布饼图数据
     * @return
     */
    List<Map<String,Object>> statUserCountRadius();

    /**
     * 用户分布轴图数据
     * @param startDateStr
     * @param endDateStr
     * @param ptLevelList
     * @return
     * @throws ParseException
     */
    List<Map<String,Object>> statUserCountAxis(String startDateStr, String endDateStr, List<Integer> ptLevelList) throws ParseException;

    /**
     * 用户分布数据统计
     * @param startDateStr
     * @param endDateStr
     * @return
     * @throws ParseException
     */
    List<StatUserCountVO> statUserCount(String startDateStr, String endDateStr) throws ParseException;

    /**
     * 日期拆分
     * @param startDateStr
     * @param endDateStr
     * @return
     * @throws ParseException
     */
    List<String> dateBreakUp(String startDateStr, String endDateStr) throws ParseException;
}
