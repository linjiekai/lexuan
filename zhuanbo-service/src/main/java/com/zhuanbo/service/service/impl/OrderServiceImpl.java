package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.CopyWritingConstants;
import com.zhuanbo.core.constants.GoodsEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.entity.Cart;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderDescribe;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.core.entity.OrderShip;
import com.zhuanbo.core.entity.StatIncomeDay;
import com.zhuanbo.core.entity.StatUserSaleDay;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.mapper.OrderMapper;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.INotifyMsgPoolService;
import com.zhuanbo.service.service.INotifyMsgService;
import com.zhuanbo.service.service.IOrderDescribeService;
import com.zhuanbo.service.service.IOrderGoodsService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IOrderShipService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.strategy.OrderProfitStrategy;
import com.zhuanbo.service.strategy.init.StrategyInit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final Long ZERO_USER_ID = 0L;

    @Autowired
    IDictionaryService iDictionaryService;
    @Autowired
    private IOrderDescribeService iOrderDescribeService;
    @Autowired
    private IOrderShipService iOrderShipService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderGoodsService iOrderGoodsService;
    @Autowired
    private INotifyMsgService iNotifyMsgService;
    @Autowired
    private INotifyMsgPoolService iNotifyMsgPoolService;
    @Autowired
    private StrategyInit strategyInit;
    @Autowired
    private AuthConfig authConfig;

    @Override
    public Page<Order> orderList(Page<Order> page, Map<String, Object> ew) {
        Optional.ofNullable(ew.get("mobile")).ifPresent(x -> {//手机号
            User user = iUserService.getOne(new QueryWrapper<User>().eq("mobile", x).eq("status", 1));
            if (user != null) {
                ew.put("mobileUserId", user.getId());
            } else {
                ew.put("mobileUserId", -999L);// 没有
            }
        });

        List<Order> orderList = baseMapper.orderList(page, ew);
        if (page == null) {
            page = new Page<>();
            page.setTotal(orderList.size());
        }
        page.setRecords(orderList);
        return page;
    }

    @Transactional
    @Override
    public void batchShip(List<String> batchShip, Integer adminId) {

        Map<String, List<OrderShip>> batchListMap = iOrderShipService.toListByOrderNo(batchShip);

        Order order;
        OrderDescribe orderDescribe;
        String order_no = "order_no";
        LocalDateTime shipTime = LocalDateTime.now();// 物流最早时间

        Set<String> uniqueOrderNoSet = new HashSet<>();
        Iterator<Map.Entry<String, List<OrderShip>>> iterator = batchListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<OrderShip>> next = iterator.next();
            String orderNo = next.getKey();
            List<OrderShip> value = next.getValue();
            if (StringUtils.isBlank(orderNo)) {
                continue;
            }
            order = getOne(new QueryWrapper<Order>().eq(order_no, orderNo));
            if (order == null) {
                throw new ShopException("", "订单号不存在");
            }
            // 校验是否可以发货（待收货、待发货才可以）
            if (!(OrderStatus.WAIT_SHIP.getId().equals(order.getOrderStatus()) || OrderStatus.WAIT_DELIVER.getId().equals(order.getOrderStatus()))) {
                log.error("订单状态不符合发货:{},{}", orderNo, order.getOrderStatus());
                throw new RuntimeException("订单状态不符合发货");
            }
            orderDescribe = iOrderDescribeService.getOne(new QueryWrapper<OrderDescribe>().eq(order_no, orderNo));
            // 待收货、待发货都可以发货，所以多次发货就已当前的为有效,旧的删除
            iOrderShipService.update(new OrderShip(), new UpdateWrapper<OrderShip>().set("deleted", 1).eq(order_no, orderNo));
            // 待收货
            order.setOrderStatus(OrderStatus.WAIT_DELIVER.getId());
            order.setUpdateTime(shipTime);
            updateById(order);
            // 更新副表信息
            orderDescribe.setShipPrice(new BigDecimal("0.0"));
            orderDescribe.setShipChannel(value.get(0).getShipChannel());
            orderDescribe.setShipSn(value.get(0).getShipSn());
            orderDescribe.setUpdateTime(shipTime);
            orderDescribe.setAdminId(adminId.longValue());
            orderDescribe.setShipTime(DateUtil.toyyyy_MM_dd_HH_mm_ss(shipTime));
            iOrderDescribeService.updateById(orderDescribe);
            // 新的记录
            for (OrderShip os : value) {
                os.setAddTime(shipTime);
                os.setUpdateTime(shipTime);
                os.setOrderStatus(OrderStatus.WAIT_DELIVER.getId());
                os.setOrderTime(DateUtil.toHH_mm_ss(shipTime));
                os.setOrderDate(DateUtil.toyyyy_MM_dd(shipTime));
                iOrderShipService.save(os);
                if (!uniqueOrderNoSet.contains(orderNo)) {
                    // 通知与推送
                    iNotifyMsgService.notifyAndPush(iUserService.getById(order.getUserId()), ConstantsEnum.PLATFORM_ZBMALL.stringValue(), 1, CopyWritingConstants.PUSH_TITLE,
                            CopyWritingConstants.ORDER_SHIP.replace("$", orderNo), MapUtil.of("type", 3, "link", ""));
                    uniqueOrderNoSet.add(orderNo);
                }
            }
        }
    }

    @Override
    public Map<String, Object> orderFinishPostProcess(String orderNo) throws Exception {

        // 订单生成方式：邀请购买，自己购买，
        Order order = getOne(new QueryWrapper<Order>().eq("order_no", orderNo));// 订单

        if (order == null) {
            return null;
        }

        if (!ckCanBeProfit(order)) {
            throw new ShopException(50007);
        }

        iUserService.removeUserCache(order.getUserId());// 清掉，不然会影响
        User user = iUserService.getById(order.getUserId());// 订单用户

        List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_no", orderNo));// 订单商品
        Long cid = iDictionaryService.findForLong(ConstantsEnum.MALL_USER.stringValue(), ConstantsEnum.COMPANY_USER_ID.stringValue());// 公司账号

        // 普通商品订单、399礼包订单、600礼包
        String orderGoodsType00 = "0:0", orderGoodsType10 = "1:0", orderGoodsType11 = "1:1";
        Map<String, List<OrderGoods>> orderGoodsCollect = orderGoodsList.stream().collect(Collectors.groupingBy(x -> x.getGoodsType() + ":" + x.getBuyerPartner(), Collectors.toList()));

        Map<String, Object> backMap;
        Integer ptLevel;
        if (CollectionUtils.isNotEmpty(orderGoodsCollect.get(orderGoodsType11))) {// 600
            ptLevel = 2;
            backMap = strategyInit.getByName(StrategyInit.GIFT6_PROFIT_DIVIDE, OrderProfitStrategy.class).orderProfit(order, cid, orderGoodsList, null);
        } else if (CollectionUtils.isNotEmpty(orderGoodsCollect.get(orderGoodsType10))) {// 399
            ptLevel = 1;
            backMap = strategyInit.getByName(StrategyInit.GIFT3_PROFIT_DIVIDE, OrderProfitStrategy.class).orderProfit(order, cid, orderGoodsList, null);
        } else if (CollectionUtils.isNotEmpty(orderGoodsCollect.get(orderGoodsType00))) {// normal
            ptLevel = 4;
            backMap = strategyInit.getByName(StrategyInit.NORMAL_PROFIT_DIVIDE, OrderProfitStrategy.class).orderProfit(order, cid, orderGoodsList, null);
        } else {
            throw new RuntimeException("未知商品分润策略");
        }

        iUserService.removeUserCache(user.getId());

        if (backMap != null) {
            // 广告池
            if (backMap.containsKey(ConstantsEnum.AD_SCROLL_LIST.stringValue())) {
                List<Map> userList = (List<Map>) backMap.get(ConstantsEnum.AD_SCROLL_LIST.stringValue());
                for (Map m : userList) {
                    User uu = (User) m.get("u");
                    uu.setPtLevel(ptLevel);
                    iNotifyMsgPoolService.simpleSave(uu, ConstantsEnum.PLATFORM_ZBMALL.stringValue(), 1, m.get("m").toString());
                }
            }
        }
        return backMap;// 推送信息
    }


    @Override
    public IPage<Order> pageCustom(Page<Order> page, Map<String, Object> ew) {

        List<Order> orders = baseMapper.pageCustom(page, ew);
        if (page == null) {
            page = new Page<>();
            page.setTotal(orders.size());
        }
        if (page.getSize() == -1) {
            page.setTotal(orders.size());
        }
        page.setRecords(orders);
        return page;
    }

    @Override
    public StatIncomeDay statUserSale(Map<String, Object> params) {
        return baseMapper.statUserSale(params);
    }

    @Override
    public List<StatUserSaleDay> listUserSale(String statDate) {
        return baseMapper.listUserSale(statDate);
    }

    @Override
    public List<Integer> giftOrderNumber(Long uid, String ignoreOrderNo) {
        return baseMapper.giftOrderNumber(uid, ignoreOrderNo);
    }

    @Override
    public boolean isLimitPrice2Oversea(List<Cart> checkedGoodsList, BigDecimal limitPrice) {
        if (CollectionUtils.isEmpty(checkedGoodsList) || limitPrice == null) {
            return true;
        }
        BigDecimal allPrice = checkedGoodsList.stream().filter(x ->
                GoodsEnum.TRACE_TYPE_1.Integer().equals(x.getTraceType()) || GoodsEnum.TRACE_TYPE_2.Integer().equals(x.getTraceType()) || GoodsEnum.TRACE_TYPE_3.Integer().equals(x.getTraceType()))
                .map(x -> x.getPrice().multiply(new BigDecimal(x.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (allPrice.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        if (allPrice.compareTo(limitPrice) == 1) {
            return false;
        }
        return true;
    }

    @Override
    public boolean haveOversea(List<Cart> checkedGoodsList) {

        long count = checkedGoodsList.stream().filter(x ->
                GoodsEnum.TRACE_TYPE_2.Integer().equals(x.getTraceType()) || GoodsEnum.TRACE_TYPE_3.Integer().equals(x.getTraceType())).count();
        if (count > 0L) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> mqMessageData(String orderNo, Integer orderType, List<Integer> typeSplit) {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("orderType", orderType);
        stringObjectHashMap.put("orderNo", orderNo);
        stringObjectHashMap.put("typeSplit", typeSplit);
        stringObjectHashMap.put("uuid", UUID.randomUUID().toString());
        return stringObjectHashMap;
    }

    @Override
    public JSONObject deductStock(String orderNo, String mobile, String userToken, String areaCode) throws Exception {

        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("mobile", mobile);
        paramsMap.put("orderNo", orderNo);
        paramsMap.put("areaCode", areaCode);
        paramsMap.put("mercId", authConfig.getMercId());
        paramsMap.put("platform", ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        paramsMap.put("sysCnl", "WEB");
        paramsMap.put("timestamp", System.currentTimeMillis() / 1000);

        String plain = Sign.getPlain(paramsMap) + "&key=" + authConfig.getMliveSignKey();
        String signServer = Sign.sign(plain);

        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put("X-MPMall-Token", userToken);
        headerMap.put("X-MPMALL-SignVer", "v1");
        headerMap.put("X-MPMALL-Sign", signServer);
        log.info("请求扣云仓参数|{}", paramsMap);
        log.info("请求扣云仓头|{}", headerMap);
        String s = HttpUtil.sendPostJson(authConfig.getStockCutUrl(), paramsMap, headerMap);
        log.info("请求扣云仓结果:{}", s);
        return JSON.parseObject(s);
    }

    @Override
    public JSONObject rollbackDeductStock(String no, String userToken) throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("no", no);
        paramsMap.put("mercId", authConfig.getMercId());
        paramsMap.put("platform", ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        paramsMap.put("sysCnl", "WEB");
        paramsMap.put("timestamp", System.currentTimeMillis() / 1000);

        String plain = Sign.getPlain(paramsMap) + "&key=" + authConfig.getMliveSignKey();
        String signServer = Sign.sign(plain);

        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put("X-MPMall-Token", userToken);
        headerMap.put("X-MPMALL-SignVer", "v1");
        headerMap.put("X-MPMALL-Sign", signServer);

        log.info("请求回退仓参数|{}", paramsMap);
        log.info("请求回退头|{}", headerMap);
        String s = HttpUtil.sendPostJson(authConfig.getStockCutRollbackUrl(), paramsMap, headerMap);
        log.info("请求回退云仓结果:{}", s);
        return JSON.parseObject(s);
    }

    /**
     * 判断是否可以分润 - 待发货、待收货、成功
     * @param order
     * @return true:可以，fales:不可以
     */
    private boolean ckCanBeProfit(Order order) {

        if (order.getOrderStatus().equals(OrderStatus.WAIT_SHIP.getId())
                || order.getOrderStatus().equals(OrderStatus.WAIT_DELIVER.getId())
                || order.getOrderStatus().equals(OrderStatus.SUCCESS.getId())){
            return true;
        }
        return false;
    }
}
