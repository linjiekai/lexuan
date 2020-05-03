package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.constants.PointTypeEnum;
import com.zhuanbo.core.dto.UserPointDetailsDTO;
import com.zhuanbo.core.entity.UserPointDetails;
import com.zhuanbo.service.mapper.UserPointDetailsMapper;
import com.zhuanbo.service.service.IUserPointDetailsService;
import com.zhuanbo.service.vo.FanChartDateVO;
import com.zhuanbo.service.vo.StatisticPointVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

/**
 * <p>
 * 用户积分明细表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
public class UserPointDetailsServiceImpl extends ServiceImpl<UserPointDetailsMapper, UserPointDetails> implements IUserPointDetailsService {


    /**
     * 分页查询积分变动明细
     *
     * @param userPointDetailsDTO
     * @param iPage
     * @return
     */
    @Override
    public List<UserPointDetailsDTO> page(IPage iPage, UserPointDetailsDTO userPointDetailsDTO) {
        return baseMapper.page(iPage, userPointDetailsDTO);
    }

    /**
     * 分页查询积分变动明细
     *
     * @return
     */
    @Override
    public Map<String, Object> statisticPoint() {
        List<FanChartDateVO> chartDateVOList = new ArrayList<>();
        List<UserPointDetails> userPointDetails = baseMapper.statisticPoint();
        Integer allPoint = 0;
        Integer usePoint = 0;
        Integer residualPoint = 0;

        // 数据统计查询
        Map<Integer, Integer> pointStatisticMap = userPointDetails.stream()
                .collect(groupingBy(UserPointDetails::getPointType, summingInt(UserPointDetails::getOperatePoint)));
        if (pointStatisticMap != null) {
            Integer depositPoint = pointStatisticMap.get(PointTypeEnum.DEPOSIT.getId());
            Integer payPoint = pointStatisticMap.get(PointTypeEnum.PAY.getId());
            if (depositPoint != null) {
                allPoint += depositPoint;
            }
            if (payPoint != null) {
                usePoint += payPoint;
            }
        }
        residualPoint = allPoint - usePoint;

        // 扇形图数据完善
        chartDateVOList.add(FanChartDateVO.builder().value(usePoint).name("已使用积分").build());
        chartDateVOList.add(FanChartDateVO.builder().value(residualPoint).name("剩余积分").build());
        // 积分数据完善
        StatisticPointVO statisticPointVO = new StatisticPointVO();
        statisticPointVO.setUsePoint(usePoint);
        statisticPointVO.setAllPoint(allPoint);
        statisticPointVO.setResidualPoint(residualPoint);

        Map<String, Object> pointMap = new HashMap<>();
        pointMap.put("chartDate", chartDateVOList);
        pointMap.put("pointDate", statisticPointVO);
        return pointMap;
    }

    /**
     * 积分日统计
     *
     * @param userPointDetailsDTO pointType
     *                            days
     * @return
     */
    @Override
    public Map<String, Object> statisticPointByDay(UserPointDetailsDTO userPointDetailsDTO) {
        Integer pointType = userPointDetailsDTO.getPointType();
        Integer days = userPointDetailsDTO.getDays();
        LocalDate addDate = LocalDate.now().minusDays(days);
        List<UserPointDetails> userPointDetailsByDay = baseMapper.statisticPointByDay(pointType, addDate);
        Map<String, Object> pointMap = new HashMap<>(userPointDetailsByDay.size());
        List<List<Object>> pointList = new ArrayList<>();
        List<Object> pointObjList;
        for (UserPointDetails userPointDetails : userPointDetailsByDay) {
            pointObjList = new ArrayList<>();
            pointObjList.add(userPointDetails.getPointDate());
            pointObjList.add(userPointDetails.getOperatePoint());
            pointList.add(pointObjList);
        }
        pointMap.put("pointData", pointList);
        return pointMap;
    }

}
