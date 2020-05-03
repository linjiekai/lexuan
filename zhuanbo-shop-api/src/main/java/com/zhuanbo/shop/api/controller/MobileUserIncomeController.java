package com.zhuanbo.shop.api.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.zhuanbo.client.server.client.PayClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.PlatformType;
import com.zhuanbo.core.constants.PurchType;
import com.zhuanbo.core.constants.TransDetailsOrderTypeEnum;
import com.zhuanbo.core.constants.TransDetailsStatusEnum;
import com.zhuanbo.core.entity.OrderTransDetails;
import com.zhuanbo.core.entity.UserIncome;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.service.IOrderTransDetailsService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.vo.UserIncomeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shop/mobile/income")
@Slf4j
public class MobileUserIncomeController {

    @Autowired
    private IUserIncomeService iUserIncomeService;

    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;

    @Autowired
    private IOrderTransDetailsService iOrderTransDetailsService;

    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private PayClient payClient;
    @Autowired
    private RestTemplate restTemplate;


    /**
     * 我的收益
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @PostMapping("/index")
    public Object index(@LoginUser Long userId, HttpServletRequest request) throws Exception {

        UserIncome userIncome = iUserIncomeService.getOne(new QueryWrapper<UserIncome>().eq("user_id", userId));
        if (userIncome == null) {
            return ResponseUtil.ok();
        }

        UserIncomeVO vo = new UserIncomeVO();
        BeanUtils.copyProperties(userIncome, vo);


        ThreadPoolExecutor threadPoolExecutor = null;
        try {
            //获取php用户数据
            String token = request.getHeader("X-MPMALL-Token");
            findUserInfoByPHP(userId,vo);

            threadPoolExecutor = new ThreadPoolExecutor(3, 3,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            String statDate = DateUtil.date10();
            List<Future<Integer>> integerList = new ArrayList<>();

            Future<Integer> submit1 = threadPoolExecutor.submit(() -> {
                index1(vo, userId, statDate);
                return 1;
            });
            integerList.add(submit1);

            Future<Integer> submit2 = threadPoolExecutor.submit(() -> {
                index2(vo, userId, statDate);
                return 1;
            });
            integerList.add(submit2);

            Future<Integer> submit4 = threadPoolExecutor.submit(() -> {
                index4(userId, vo);
                return 1;
            });
            integerList.add(submit4);

            for (Future<Integer> integerFuture : integerList) {
                Integer integer = integerFuture.get();
            }
        } catch (Exception e) {
            log.error("首页异常：{}", e);
            throw new RuntimeException(e);
        } finally {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdown();
            }
        }
        return ResponseUtil.ok(vo);
    }


    /**
     * 我的余额
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @PostMapping("/bal")
    public Object bal(@LoginUser Long userId) throws Exception {
        UserIncomeVO vo = new UserIncomeVO();
        index4(userId, vo);
        return ResponseUtil.ok(vo);
    }


    // 今日总收益
    private void index1(UserIncomeVO vo, Long userId, String statDate) {
        List<UserIncomeDetails> todayList = iUserIncomeDetailsService.list(
                new QueryWrapper<UserIncomeDetails>().eq("income_date", statDate).eq("user_id", userId)
                        .in("income_type", Arrays.asList(1, 2, 3, 4, 5, 6)).eq("status", 1));
        BigDecimal todayUavaIncome = new BigDecimal(0);
        BigDecimal todayWithdrawIncome = new BigDecimal(0);
        List<UserIncomeDetails> unCount = todayList.stream().filter(s -> s.getStatType().intValue() == 0).collect(Collectors.toList());
        List<UserIncomeDetails> count = todayList.stream().filter(s -> s.getStatType().intValue() == 1).collect(Collectors.toList());
        //今日在途收益
        for (UserIncomeDetails s : unCount) {
            if (s.getOperateType().intValue() == 1) {
                todayUavaIncome = todayUavaIncome.add(s.getOperateIncome());
            } else {
                todayUavaIncome = todayUavaIncome.subtract(s.getOperateIncome());
            }
        }
        //今日可提收益
        for (UserIncomeDetails s : count) {
            if (s.getOperateType().intValue() == 1) {
                todayWithdrawIncome = todayWithdrawIncome.add(s.getOperateIncome());
            } else {
                todayWithdrawIncome = todayWithdrawIncome.subtract(s.getOperateIncome());
            }
        }
        vo.setTodayUavaIncome(todayUavaIncome); // 今日在途收益
        vo.setTodayWithdrawIncome(todayWithdrawIncome); // 今日可提收益
        vo.setTodayTotalIncome(todayUavaIncome.add(todayWithdrawIncome)); //今日总收益
    }

    //累计
    private void index2(UserIncomeVO vo, Long userId, String statDate) {
        List<Integer> ids = CollUtil.newArrayList();

        if(CollUtil.isNotEmpty(vo.getTeamList())){
            ids = vo.getTeamList().stream().map(s -> s.getId()).collect(Collectors.toList());
        }
        ids.add(userId.intValue());

        List<OrderTransDetails> orderTransDetailsList = iOrderTransDetailsService.list(new QueryWrapper<OrderTransDetails>()
                .in("user_id", ids)
                .in("purch_type", Lists.newArrayList(PurchType.BUY.getId())));

        BigDecimal todayTotalConsume = new BigDecimal(0);// 今日累计销售
        BigDecimal totalConsume = new BigDecimal(0);//累计销售
        Integer todayTotalBuy = 0;// 今日订购数
        Integer totalBuy = 0;//总订购数
        if (CollectionUtils.isNotEmpty(orderTransDetailsList)) {
            for (OrderTransDetails orderTransDetails : orderTransDetailsList) {
                if (TransDetailsStatusEnum.EFFECTIVE.getId() != orderTransDetails.getStatus().intValue()) {
                    continue;
                }

                if (TransDetailsOrderTypeEnum.TRADE.getId() == orderTransDetails.getOrderType()) {
                    if (orderTransDetails.getOperateType().equals(1)) {
                        totalConsume = totalConsume.add(orderTransDetails.getPrice());
                        totalBuy += orderTransDetails.getBuyNum();
                        if (statDate.equalsIgnoreCase(orderTransDetails.getTransDate())) {
                            todayTotalConsume = todayTotalConsume.add(orderTransDetails.getPrice());
                            todayTotalBuy += orderTransDetails.getBuyNum();
                        }
                    } else {
                        totalConsume = totalConsume.subtract(orderTransDetails.getPrice());
                        totalBuy -= orderTransDetails.getBuyNum();
                        if (statDate.equalsIgnoreCase(orderTransDetails.getTransDate())) {
                            todayTotalConsume = todayTotalConsume.add(orderTransDetails.getPrice());
                            todayTotalBuy += orderTransDetails.getBuyNum();
                        }
                    }
                } else if (TransDetailsOrderTypeEnum.REFUND.getId() == orderTransDetails.getOrderType()) {
                    totalConsume = totalConsume.subtract(orderTransDetails.getPrice());
                    totalBuy -= orderTransDetails.getBuyNum();
                    if (statDate.equalsIgnoreCase(orderTransDetails.getTransDate())) {
                        todayTotalConsume = todayTotalConsume.add(orderTransDetails.getPrice());
                        todayTotalBuy += orderTransDetails.getBuyNum();
                    }

                }
            }
        }
        vo.setTodayTotalConsume(todayTotalConsume.setScale(2, BigDecimal.ROUND_HALF_UP));// 今日累计销售
        vo.setTodayTotalBuy(todayTotalBuy); //今日总订购数
        vo.setTotalBuy(totalBuy); //总订购数
        vo.setTotalConsume(totalConsume.setScale(2, BigDecimal.ROUND_HALF_UP)); //累计销售
    }

    private void index4(Long userId, UserIncomeVO vo) throws Exception {

        Map<String, Object> params = new HashMap<>();
        // 用户余额
        params.put("methodType", "QueryBalance");
        params.put("userId", userId);
        params.put("mercId", PlatformType.ZBMALL.getId());
        params.put("platform", PlatformType.ZBMALL.getCode());
        params.put("requestId", System.currentTimeMillis());

        String sign = Sign.sign(params, authConfig.getMercPrivateKey());

        MDC.put("X-MPMALL-Sign-PAY", sign);
        log.info("QueryBalance request：{}", JacksonUtil.objTojson(params));
        ResponseDTO unified = payClient.unified(params);
        log.info("QueryBalance response：{}", JacksonUtil.objTojson(unified));

        if (unified == null) {
            return;
        }
        String code = unified.getCode().trim();
        String msg = unified.getMsg().trim();
        if (!"10000".equalsIgnoreCase(code)) {
            throw new ShopException(code, msg);
        }
        Map<String, Object> map = (Map<String, Object>) unified.getData();
        vo.setAcBal(new BigDecimal(map.get("acBal").toString()));
        vo.setUavaBal(new BigDecimal(map.get("uavaBal").toString()));
        vo.setWithdrBal(new BigDecimal(map.get("withdrBal").toString()));
    }


    private void findUserInfoByPHP(Long userId,UserIncomeVO vo) throws Exception{
        HashMap<String, Object> paramsMap = MapUtil.newHashMap();
        paramsMap.put("mercId",PlatformType.XFYLMALL.getId());
        paramsMap.put("platform",PlatformType.XFYLMALL.getCode());
        paramsMap.put("sysCnl","WEB");
        paramsMap.put("timestamp",System.currentTimeMillis() / 1000);
        paramsMap.put("userId",userId);

        Map map = restTemplate.postForObject(authConfig.getPhpUserUrl(), JSONUtil.toJsonStr(paramsMap), Map.class);
        Optional.ofNullable(map).ifPresent(s -> {
            if (10000 == (Integer) s.get("code")) {
                Map<String, Object> data = (Map<String, Object>) s.get("data");
                vo.setTotalTeam((Integer) data.get("totalNum")); //总客户数
                vo.setTodayTotalTeam((Integer) data.get("addNum")); //今日客户数
                String list = JSONUtil.toJsonStr(data.get("teamList"));
                JSONArray objects = JSONUtil.parseArray(list);
                List<UserIncomeVO.teamUser> teamList = JSONUtil.toList(objects, UserIncomeVO.teamUser.class);
                vo.setTeamList(teamList);
            }
        });

    }
}
