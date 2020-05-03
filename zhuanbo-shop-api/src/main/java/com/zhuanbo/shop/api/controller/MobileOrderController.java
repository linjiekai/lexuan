package com.zhuanbo.shop.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.annotation.ResponseLog;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.BuyTypeEnum;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.OrderTypeEnum;
import com.zhuanbo.core.constants.PurchType;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.constants.UserIncomeStatusType;
import com.zhuanbo.core.entity.Address;
import com.zhuanbo.core.entity.Cart;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderDescribe;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.core.entity.OrderShip;
import com.zhuanbo.core.entity.Product;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.exception.StockException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAddressService;
import com.zhuanbo.service.service.ICartService;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IGoodsCarriageService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IOrderDescribeService;
import com.zhuanbo.service.service.IOrderGoodsService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IOrderShipService;
import com.zhuanbo.service.service.IProductService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserBuyerService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.utils.OrderHandleOption;
import com.zhuanbo.service.utils.OrderUtil;
import com.zhuanbo.shop.api.dto.req.OrderParamsDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/*
 * 订单设计
 *
 * 订单状态：
 * 101 订单生成，未支付；102，下单后未支付用户取消；103，下单后未支付超时系统自动取消
 * 201 支付完成，商家未发货；202，订单生产，已付款未发货，但是退款取消；
 * 301 商家发货，用户未确认；
 * 401 用户确认收货，订单结束； 402 用户没有确认收货，但是快递反馈已收获后，超过一定时间，系统自动确认收货，订单结束。
 *
 * 当101用户未付款时，此时用户可以进行的操作是取消订单，或者付款操作
 * 当201支付完成而商家未发货时，此时用户可以取消订单并申请退款
 * 当301商家已发货时，此时用户可以有确认收货的操作
 * 当401用户确认收货以后，此时用户可以进行的操作是删除订单，评价商品，或者再次购买
 * 当402系统自动确认收货以后，此时用户可以删除订单，评价商品，或者再次购买
 *
 * 目前不支持订单退货和售后服务
 *
 */
@RestController
@RequestMapping("/shop/mobile/order")
@Validated
@Slf4j
@ResponseLog
@RefreshScope
public class MobileOrderController {

    @Value("${limit.order-submit}")
    private String limitOrderSubmit;

    @Autowired
    private IUserService userService;
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderDescribeService iShopOrderDescribeService;
    @Autowired
    private IOrderGoodsService orderGoodsService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private ICartService cartService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IOrderShipService iOrderShipService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    private IUserBuyerService iUserBuyerService;
    @Autowired
    private IGoodsCarriageService iGoodsCarriageService;
    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private ICashService iCashService;

    /**
     * 订单列表
     *
     * @param userId         用户ID
     * @param orderParamsDTO 订单信息
     *                       0， 全部订单
     *                       1，待付款
     *                       2，待发货
     *                       3，待收货
     *                       4，待评价
     * @return 订单操作结果
     */
    @PostMapping("/list")
    public Object list(@LoginUser Long userId, @RequestBody OrderParamsDTO orderParamsDTO) {

        Map<String, Object> ew = new HashMap<>();
        ew.put("userId", userId);
        ew.put("now", System.currentTimeMillis());
        ew.put("orderStatus", orderParamsDTO.getStatus());
        IPage<Order> orderIPage = orderService.pageCustom(new Page<>(orderParamsDTO.getPage(), orderParamsDTO.getLimit()), ew);

        List<Map<String, Object>> orderVoList = new ArrayList<>();
        for (Order order : orderIPage.getRecords()) {
            Map<String, Object> orderVo = new HashMap<>();
            orderVo.put("id", order.getId());
            orderVo.put("orderNo", order.getOrderNo());
            orderVo.put("status", order.getOrderStatus());
            orderVo.put("orderStatusText", OrderStatus.parse(order.getOrderStatus()).getName());
            orderVo.put("totalPrice", order.getTotalPrice());
            orderVo.put("handleOption", OrderUtil.build(order));
            List<OrderGoods> orderGoodsList = orderGoodsService.list(
                    new QueryWrapper<OrderGoods>().eq("order_no", order.getOrderNo()));
            List<Map<String, Object>> orderGoodsVoList = new ArrayList<>(orderGoodsList.size());
            for (OrderGoods orderGoods : orderGoodsList) {
                Map<String, Object> orderGoodsVo = new HashMap<>();
                orderGoodsVo.put("goodsId", orderGoods.getGoodsId());
                orderGoodsVo.put("productId", orderGoods.getProductId());
                orderGoodsVo.put("goodsName", orderGoods.getGoodsName());
                orderGoodsVo.put("specifications", orderGoods.getSpecifications());
                orderGoodsVo.put("number", orderGoods.getNumber());
                orderGoodsVo.put("picUrl", orderGoods.getPicUrl());
                orderGoodsVo.put("price", orderGoods.getPrice());
                orderGoodsVo.put("goodsType", orderGoods.getGoodsType());
                orderGoodsVoList.add(orderGoodsVo);
            }
            orderVo.put("goodsList", orderGoodsVoList);
            orderVoList.add(orderVo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", orderIPage.getTotal());
        result.put("items", orderVoList);
        return ResponseUtil.ok(result);
    }

    /**
     * 订单详情
     *
     * @param userId 用户ID
     * @param body   订单信息
     * @return 订单操作结果
     * 成功则
     */
    @PostMapping("/detail")
    public Object detail(@LoginUser Long userId, @RequestBody String body) {

        String orderNo = JacksonUtil.parseString(body, "orderNo");
        // 订单信息
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        OrderDescribe orderDescribe = iShopOrderDescribeService.getOne(new QueryWrapper<OrderDescribe>().eq("order_no", orderNo));
        if (order == null) {
            return ResponseUtil.result(50001);
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.result(50002);
        }
        Map<String, Object> orderVo = new HashMap<String, Object>();
        orderVo.put("id", order.getId());
        orderVo.put("orderNo", order.getOrderNo());
        orderVo.put("status", order.getOrderStatus());
        orderVo.put("orderStatusText", OrderStatus.parse(order.getOrderStatus()).getName());
        orderVo.put("name", orderDescribe.getContactsName());
        orderVo.put("mobile", orderDescribe.getMobile());
        orderVo.put("address", orderDescribe.getAddress());
        orderVo.put("totalPrice", order.getTotalPrice());
        orderVo.put("price", order.getPrice());
        orderVo.put("goodsTotalPrice", orderDescribe.getGoodsTotalPrice());
        orderVo.put("shipPrice", orderDescribe.getShipPrice());
//      orderVo.put("handleOption", OrderUtil.build(order));
        orderVo.put("addTime", order.getAddTime());
        orderVo.put("payNo", order.getPayNo());
        orderVo.put("payTime", order.getPayDate() + " " + order.getPayTime());
        orderVo.put("shipTime", orderDescribe.getShipTime());
        orderVo.put("buyType", order.getBuyType());
        orderVo.put("couponSn", orderDescribe.getCouponSn());
        orderVo.put("reducePrice", order.getReducePrice());

        long expTime;
        if (order.getExpTime() == null || order.getExpTime().equals(0L)) {
            LocalDateTime plus = order.getAddTime().plus(24, ChronoUnit.HOURS);
            expTime = plus.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            expTime = order.getExpTime();
        }

        LocalDateTime now = LocalDateTime.now();
        long expTimeNow = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();// 当前过了多久
        orderVo.put("expTime", expTimeNow < expTime ? (expTime - expTimeNow) / 1000 : -1);

        List<OrderGoods> orderGoodsList = orderGoodsService.list(
                new QueryWrapper<OrderGoods>().eq("order_no", order.getOrderNo()));
        List<Map<String, Object>> orderGoodsVoList = new ArrayList<>();
        for (OrderGoods orderGoods : orderGoodsList) {
            Map<String, Object> orderGoodsVo = new HashMap<>();
            orderGoodsVo.put("goodsId", orderGoods.getGoodsId());
            orderGoodsVo.put("productId", orderGoods.getProductId());
            orderGoodsVo.put("goodsName", orderGoods.getGoodsName());
            orderGoodsVo.put("specifications", orderGoods.getSpecifications());
            orderGoodsVo.put("number", orderGoods.getNumber());
            orderGoodsVo.put("picUrl", orderGoods.getPicUrl());
            orderGoodsVo.put("price", orderGoods.getPrice());
            orderGoodsVo.put("categoryId", orderGoods.getCategoryId());
            orderGoodsVoList.add(orderGoodsVo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderInfo", orderVo);
        result.put("orderGoods", orderGoodsVoList);
        return ResponseUtil.ok(result);

    }

    /**
     * 提交订单
     * 1. 根据地址ID，创建订单表项
     * 2. 购物车清空
     * 4. 商品货品数量减少
     *
     * @param userId         用户ID
     * @param orderParamsDTO 订单信息，{ cartId：xxx, addressId: xxx, couponId: xxx }
     * @return 订单操作结果
     */
    @PostMapping("/submit")
    public Object submit(@LoginUser Long userId, @RequestBody OrderParamsDTO orderParamsDTO, HttpServletRequest request) throws Exception{

        if ("1".equalsIgnoreCase(limitOrderSubmit)) {
            if (!BuyTypeEnum.BUY_TYPE_2.value().equals(orderParamsDTO.getBuyType())) {
                return ResponseUtil.result(50014);
            }
        }

        Integer addressId = orderParamsDTO.getAddressId();
        String sysCnl = orderParamsDTO.getSysCnl();
        String clientIp = orderParamsDTO.getClientIp();
        List<Integer> cartIds = orderParamsDTO.getCartIds();
        Long inviteUserId = null;// 邀请人
        if (addressId == null || sysCnl == null || clientIp == null || cartIds == null) {
            return ResponseUtil.badArgument();
        }

        Integer buyType1 = 1;// 分享购买
        if (buyType1.equals(orderParamsDTO.getBuyType()) && StringUtils.isBlank(orderParamsDTO.getInviteCode())) {
            return ResponseUtil.badArgument();
        }

        User user = userService.getById(userId);
        if (user == null) {
            return ResponseUtil.result(10007);
        }
        // 收货地址
        Address checkedAddress = addressService.getById(addressId);
        if (checkedAddress == null) {
            return ResponseUtil.result(40003);
        }
        // 货品价格
        List<Cart> checkedGoodsList = (List<Cart>) cartService.listByIds(cartIds);

        if (checkedGoodsList == null || checkedGoodsList.size() == 0) {
            return ResponseUtil.result(40002);
        }
        
        /*String appVersion = request.getHeader("X-MPMALL-APPVer");
        if (StringUtils.isBlank(appVersion)) {
        	appVersion = request.getHeader("X-MP-APPVer");
        }
        */
        ///log.info("appVersion ======= " + appVersion);
        // 暂时没有
        /*if (!StringUtils.isBlank(appVersion) && appVersion.compareTo("1.4") >= 0) {
        	//获取最大的贸易方式TraceType
            Integer maxTraceType = cartService.maxTraceType(checkedGoodsList);
            
            // 海外商品价格限制
            if (maxTraceType > 0) {
                String limitPrice = iDictionaryService.findForString(ConstantsEnum.PRICE.stringValue(), ConstantsEnum.LIMIT_OVERSEA.stringValue());
                boolean limitPrice2Oversea = orderService.isLimitPrice2Oversea(checkedGoodsList, new BigDecimal(limitPrice));
                if (!limitPrice2Oversea) {
                    return ResponseUtil.result(40005);
                }
                // 订购人信息
                UserBuyer userBuyer = iUserBuyerService.getById(orderParamsDTO.getUserBuyerId());
                if (userBuyer == null || ConstantsEnum.DELETED_1.integerValue().equals(userBuyer.getDeleted())) {
                    return ResponseUtil.result(40006);
                }
                
                if (StringUtils.isBlank(userBuyer.getName()) || StringUtils.isBlank(userBuyer.getCardNo())) {
                    return ResponseUtil.result(40006);
                }
                
                if (!userBuyer.getName().equals(checkedAddress.getName())) {
                    return ResponseUtil.result(40006);
                }
                
                if (maxTraceType == TraceType.TraceType_3.getId()) {
                	if (StringUtils.isBlank(userBuyer.getImgFront()) || StringUtils.isBlank(userBuyer.getImgBack())) {
                		return ResponseUtil.result(40006);
                	}
                }
                
            }

        } else {
        	//获取最大的贸易方式TraceType
            Integer maxTraceType = cartService.maxTraceType(checkedGoodsList);
            
            // 海外商品价格限制
            if (maxTraceType > 0) {
            	return ResponseUtil.fail(40006, "请更新至最新App版本进行购买！");
            }
        	
        }*/

        List<Cart> lsCart = new ArrayList<>();
        lsCart.addAll(checkedGoodsList);
        lsCart.sort(Comparator.comparing(Cart::getProductId).reversed());// 按顺序来，避死锁

        Product product = null;
        //Goods goods = null;

        // 订单费用
        BigDecimal orderTotalPrice = null;
        // 商品费用
        BigDecimal goodsTotalPrice = new BigDecimal(0.00);
        // 运费
        Integer productCount = lsCart.stream().map(Cart::getNumber).reduce(0, Integer::sum);
        // 赚品运费保持不变
        BigDecimal shipPrice = BuyTypeEnum.BUY_TYPE_2.value().equals(orderParamsDTO.getBuyType()) ? iGoodsCarriageService.calculateShipping(productCount) : BigDecimal.ZERO;
        if (StringUtils.isNotBlank(orderParamsDTO.getInviteCode())) {
            User inviteUser = userService.getOne(new QueryWrapper<User>().eq("invite_code", orderParamsDTO.getInviteCode()));
            if (inviteUser != null && !ConstantsEnum.USER_PT_LEVEL_0.integerValue().equals(inviteUser.getPtLevel())) {
                inviteUserId = inviteUser.getId();
            }
        }

        // 开启事务管理
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        Order order = null;
        OrderDescribe orderDescribe = null;
        String prepayNo = null;
        String userToken = request.getHeader("X-MPMALL-Token");
        List<String> cutNoList = new ArrayList<>();
        String couponSn = null;
        //User receiveGiftUser = userService.getOne(new QueryWrapper<User>().eq("mobile", orderParamsDTO.getReceiveGiftMobile()).eq("deleted", 0));
        try {

            String orderNo = DateUtil.date8() + iSeqIncrService.nextVal("order_no", 8, Align.LEFT);
            // 商品货品数量减少
            for (Cart cart : lsCart) {
                // 商品失效校验
                iGoodsService.checkGoodsStatus(cart.getGoodsId());
                // 扣
                if (BuyTypeEnum.BUY_TYPE_2.value().equals(orderParamsDTO.getBuyType())) {
                    if (StringUtils.isBlank(orderParamsDTO.getAreaCode())) {
                        User receiveGiftUser = userService.getOne(new QueryWrapper<User>().eq("mobile", orderParamsDTO.getReceiveGiftMobile()).eq("deleted", 0));
                        orderParamsDTO.setAreaCode(receiveGiftUser.getAreaCode());
                    }
                    JSONObject result = orderService.deductStock(orderNo, orderParamsDTO.getReceiveGiftMobile(), userToken, orderParamsDTO.getAreaCode());
                    if (result.getString("code").equals("10000")) {
                        cutNoList.add(result.getJSONObject("data").getString("no"));
                        couponSn = result.getJSONObject("data").getString("no");
                    } else {
                        throw new StockException("扣减云仓失败:" + result.getString("msg"));
                    }
                }
                // 订单商品
                OrderGoods orderGoods = new OrderGoods();
                orderGoods.setOrderNo(orderNo);
                orderGoods.setGoodsId(cart.getGoodsId());
                orderGoods.setGoodsSn(cart.getGoodsSn());
                orderGoods.setProductId(cart.getProductId());
                orderGoods.setGoodsName(cart.getGoodsName());
                orderGoods.setPicUrl(cart.getPicUrl());
                orderGoods.setPrice(cart.getPrice());
                orderGoods.setNumber(cart.getNumber());
                orderGoods.setGoodsType(cart.getGoodsType());
                orderGoods.setSharePrice(cart.getSharePrice());
                orderGoods.setTraceType(cart.getTraceType());
                orderGoods.setCategoryId(cart.getCategoryId());
                List<Map<String, Object>> specifications = cart.getSpecifications();
                List<String> collect = specifications.stream().map(s -> s.get("name") + "").collect(Collectors.toList());
                String[] strings1 = collect.toArray(new String[collect.size()]);
                orderGoods.setSpecifications(strings1);

                // 添加订单商品表项
                if (!orderGoodsService.save(orderGoods)) {
                    log.error("下单保存商品失败,userId={}, productId={}, goodsId={}, goodsName={}", userId, cart.getProductId(), cart.getGoodsId(), cart.getGoodsName());
                    throw new ShopException(50005, "下单保存商品失败,商品名称=" + cart.getGoodsName());
                }
                // 删除购物车里面的商品信息
                if (!cartService.removeById(cart.getId())) {
                    log.error("清除购物车商品失败,userId={}, productId={}, goodsId={}, goodsName={}", userId, cart.getProductId(), cart.getGoodsId(), cart.getGoodsName());
                    throw new ShopException(50005, "清除购物车商品失败,商品名称=" + cart.getGoodsName());
                }

                //累加商品金额
                goodsTotalPrice = goodsTotalPrice.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())).divide(new BigDecimal(authConfig.getGoodsNumber()), 2, BigDecimal.ROUND_HALF_UP));

            }
            //目前没有优惠券，只算运费
            orderTotalPrice = goodsTotalPrice.add(shipPrice);

            //计算失效时间 15分钟
            Long expTime = DateUtil.getPeriodTime(15, "00").getTime();

            // 订单
            order = new Order();
            orderDescribe = new OrderDescribe();
            order.setMercId(authConfig.getMercId());
            order.setUserId(userId);
            order.setUserName(user.getUserName());
            order.setNickname(user.getNickname());
            order.setSysCnl(sysCnl);
            order.setClientIp(clientIp);
            order.setOrderNo(orderNo);
            order.setTotalPrice(orderTotalPrice);
            order.setPrice(orderTotalPrice);
            order.setOrderDate(DateUtil.LocalDateTimeToString(LocalDateTime.now(), "yyyy-MM-dd"));
            order.setOrderTime(DateUtil.LocalDateTimeToString(LocalDateTime.now(), "HH:mm:ss"));
            order.setExpTime(expTime);
            order.setBuyType(orderParamsDTO.getBuyType());
            order.setPlatform(orderParamsDTO.getPlatform());
            order.setInviteUserId(inviteUserId);
            order.setPurchType(PurchType.BUY.getId());
            order.setOrderType(BuyTypeEnum.BUY_TYPE_2.value().equals(orderParamsDTO.getBuyType()) ? OrderTypeEnum.BUY_ORDER_TYPE_1.value() : OrderTypeEnum.BUY_ORDER_TYPE_0.value());
            order.setPtLevel(user.getPtLevel());
            order.setOrderStatus(OrderStatus.WAIT_PAY.getId());
            //订单副表
            orderDescribe.setOrderNo(orderNo);
            orderDescribe.setGoodsTotalPrice(orderTotalPrice.subtract(shipPrice));// 不计算运费
            orderDescribe.setShipPrice(shipPrice);
            orderDescribe.setContactsName(checkedAddress.getName());
            orderDescribe.setMobile(checkedAddress.getMobile());
            orderDescribe.setProvinceId(checkedAddress.getProvinceId());
            orderDescribe.setCityId(checkedAddress.getCityId());
            orderDescribe.setAreaId(checkedAddress.getAreaId());
            orderDescribe.setCountryId(checkedAddress.getCountryId());
            orderDescribe.setAddress(addressService.detailedAddress(checkedAddress));
            orderDescribe.setUserBuyerId(orderParamsDTO.getUserBuyerId());
            orderDescribe.setReceiveGiftMobile(orderParamsDTO.getReceiveGiftMobile());
            orderDescribe.setCouponSn(couponSn);

            // 添加订单表项
            if (!orderService.save(order) || !iShopOrderDescribeService.save(orderDescribe)) {
                log.error("生成订单失败,userId={}, orderNo", userId, orderNo);
                throw new ShopException(50005, "生成订单失败");
            }
            txManager.commit(status);
        } catch (Exception ex) {
            txManager.rollback(status);
            log.error("下单失败", ex);
            // 回退库存
            for (String no : cutNoList) {
                try {
                    JSONObject result = orderService.rollbackDeductStock(no, userToken);
                    if (!result.getString("code").equals("10000")) {
                        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
                        stringObjectHashMap.put("no", no);
                        stringObjectHashMap.put("userToken", userToken);
                        iRabbitMQSenderService.send(RabbitMQSenderImpl.ROLL_BACK_DEDUCT_STOCK, stringObjectHashMap);
                    }
                } catch (Exception e) {
                    log.error("下单回退云仓失败|no|{}|{}", no, e);
                }
            }
            if (ex instanceof StockException) {
                StockException stockException = (StockException) ex;
                return ResponseUtil.fail(50005, stockException.getMessage());
            }
            return ResponseUtil.result(50005);
        }
        Map<String, Object> backMap = new HashMap<>();
        // 赠品不走支付
        if (BuyTypeEnum.BUY_TYPE_2.value().equals(orderParamsDTO.getBuyType()) && BigDecimal.ZERO.compareTo(orderTotalPrice) == 0) {
            prepayNo = "";
            // 状态修改统一处理
            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put("orderNo", order.getOrderNo());
            iRabbitMQSenderService.send(RabbitMQSenderImpl.LOCAL_ORDER, stringObjectHashMap);
        } else {
            prepayNo= submitPayInfo(user,order);
        }
        backMap.put("mercId", order.getMercId());
        backMap.put("prepayNo", prepayNo);
        backMap.put("orderNo", order.getOrderNo());
        return ResponseUtil.ok(backMap);
    }

    /**
     * 完成下单后将支付信息发送到支付平台
     *
     * @param order
     */
    public String submitPayInfo(User user, Order order) throws Exception {
        String prepayNo = "";
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("methodType", "DirectPrePay");
            params.put("callbackUrl", "");
            params.put("notifyUrl", authConfig.getNotifyUrl());
            params.put("mercId", order.getMercId());
            params.put("requestId", System.currentTimeMillis());
            params.put("orderNo", order.getOrderNo());
            params.put("orderDate", order.getOrderDate());
            params.put("orderTime", order.getOrderTime());
            params.put("price", order.getPrice());
            params.put("sysCnl", order.getSysCnl());
            params.put("userId", String.valueOf(user.getId()));
            params.put("period", 1);
            params.put("periodUnit", "02");
            params.put("userId", String.valueOf(user.getId()));
            params.put("mobile", user.getMobile());
            params.put("tradeCode", "02");
            params.put("busiType", "02");
            params.put("clientIp", order.getClientIp());
            params.put("platform", order.getPlatform());
            JSONObject send = iCashService.send(params);
            String code = send.getString(ReqResEnum.CODE.String());
            String msg = send.getString(ReqResEnum.MSG.String());
            if (null == send) {
                log.error("统一下单调用失败");
                throw new ShopException(50009);
            }
            //如果返回码不成功;
            if (StringUtils.isBlank(code) || !Constants.SUCCESS_CODE.equals(code)) {
                log.error("统一下单调用失败:code：{}, msg：{}", code, msg);
                throw new ShopException(code, msg);
            }
            JSONObject jsonObject = send.getJSONObject(ReqResEnum.DATA.String());
            prepayNo = jsonObject.getString("prePayNo");
        } catch (Exception e) {
            log.error("下单失败", e);
            throw new ShopException(50005);
        }
        return prepayNo;
    }

    /**
     * 取消订单
     * 1. 检测当前订单是否能够取消
     * 2. 设置订单取消状态
     * 3. 商品货品数量增加
     *
     * @param userId         用户ID
     * @param orderParamsDTO 订单信息，{ orderId：xxx }
     * @return 订单操作结果
     * 成功则 { errno: 0, errmsg: '成功' }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("/cancel")
    public Object cancel(@LoginUser Long userId, @RequestBody OrderParamsDTO orderParamsDTO) {

        String orderNo = orderParamsDTO.getOrderNo();
        // 订单信息
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) {
            return ResponseUtil.result(50001);
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.result(50002);
        }

        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isCancel()) {
            return ResponseUtil.result(50003);
        }

        // 开启事务管理
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        try {
            // 设置订单已取消状态
            order.setOrderStatus(OrderStatus.CANCEL.getId());
            orderService.updateById(order);

            // 释放商品库存
            /*List<OrderGoods> orderGoodsList = orderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_no",order.getOrderNo()));
            for (OrderGoods orderGoods : orderGoodsList) {
                Integer productId = orderGoods.getProductId();
                Product product = productService.getById(productId);
                productService.updateById(product);
            }*/
        } catch (Exception ex) {
            txManager.rollback(status);
            log.error("系统内部错误", ex);
            return ResponseUtil.result(50004);
        }
        txManager.commit(status);

        return ResponseUtil.ok();
    }

    /**
     * 删除订单
     * 1. 检测当前订单是否删除
     * 2. 设置订单删除状态
     *
     * @param userId         用户ID
     * @param orderParamsDTO 订单信息，{ orderId：xxx }
     * @return 订单操作结果
     * 目前暂时没有使用订单删除接口
     */
    @PostMapping("/delete")
    public Object delete(@LoginUser Long userId, @RequestBody OrderParamsDTO orderParamsDTO) {

        String orderNo = orderParamsDTO.getOrderNo();

        // 订单信息
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) {
            return ResponseUtil.result(50001);
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.result(50002);
        }
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isDelete()) {
            return ResponseUtil.result(50006);
        }

        // 订单order_status没有字段用于标识删除
        // 而是存在专门的delete字段表示是否删除
//        order.setOrderStatus(OrderUtil.);
        orderService.updateById(order);
        return ResponseUtil.ok();
    }

    /**
     * 订单完成
     *
     * @param userId
     * @param orderParamsDTO
     * @return
     */
    @Transactional
    @PostMapping("/finish")
    public Object orderFinish(@LoginUser Long userId, @RequestBody OrderParamsDTO orderParamsDTO) {

        String orderNo = orderParamsDTO.getOrderNo();
        // 订单信息
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) {
            return ResponseUtil.result(50001);
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.result(50002);
        }
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isConfirm()) {
            return ResponseUtil.result(50012);
        }

        // 订单order_status没有字段用于标识删除
        // 而是存在专门的delete字段表示是否删除
        order.setOrderStatus(OrderStatus.SUCCESS.getId());
        orderService.updateById(order);

        // 在途收益、可提收益作相对应的处理
        List<UserIncomeDetails> userIncomeDetailsList = iUserIncomeDetailsService.list(new QueryWrapper<UserIncomeDetails>().eq("order_no", order.getOrderNo()).eq("status", UserIncomeStatusType.NORMAL.getId()));
        List<Long> userIdList = userIncomeDetailsList.stream().map(UserIncomeDetails::getUserId).collect(Collectors.toList());
        userIdList.add(order.getUserId());
        userIdList.forEach(uid -> iRabbitMQSenderService.send(RabbitMQSenderImpl.INCOME_CHANGE_DEPOSIT, uid));

        return ResponseUtil.ok();
    }

    /**
     * 重新付款信息确认
     *
     * @param userId
     * @param orderParamsDTO
     * @return
     * @throws Exception
     */
    @PostMapping("/repay")
    public Object repay(@LoginUser Long userId, @RequestBody OrderParamsDTO orderParamsDTO) throws Exception {

        // 订单信息
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderParamsDTO.getOrderNo()));
        if (order == null) {
            return ResponseUtil.result(50001);
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.result(50002);
        }

        User user = userService.getById(userId);
        if (user == null) {
            return ResponseUtil.result(10007);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("mercId", order.getMercId());
        data.put("prepayNo", submitPayInfo(user, order));
        data.put("orderNo", order.getOrderNo());
        return ResponseUtil.ok(data);
    }

    /**
     * @param uid
     * @param orderParamsDTO
     * @return
     */
    @PostMapping("/ship/trace")
    public Object shipTrace(@LoginUser Long uid, @RequestBody OrderParamsDTO orderParamsDTO) {

        List<Object> result = new ArrayList<>();

        QueryWrapper<OrderShip> orderShipQueryWrapper = new QueryWrapper<>();
        orderShipQueryWrapper.eq("order_no", orderParamsDTO.getOrderNo());
        orderShipQueryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        if (StringUtils.isNotBlank(orderParamsDTO.getShipSn())) {
            orderShipQueryWrapper.eq("ship_sn", orderParamsDTO.getShipSn());
        }

        List<OrderShip> orderShipList = iOrderShipService.list(orderShipQueryWrapper);
        if (!orderShipList.isEmpty()) {

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + authConfig.getShipAppCode());
            String query;
            List<JSONObject> detailList;

            for (OrderShip orderShip : orderShipList) {

                if (!OrderStatus.SUCCESS.getId().equalsIgnoreCase(orderShip.getOrderStatus())) {
                    query = HttpUtil.sendGet(authConfig.getShipTraceUrl(), "comid=" + orderShip.getShipChannel()
                            + "&number=" + orderShip.getShipSn(), headers);
                    log.info("第三方物流：{}", query);
                    detailList = iOrderShipService.toDetailList(query);
                    if (detailList.isEmpty()) {
                        continue;
                    }
                    if (iOrderShipService.isSuccessbyDetail(detailList)) {
                        // 更新物流数据
                        orderShip.setOrderStatus(OrderStatus.SUCCESS.getId());
                        orderShip.setRouteInfo(JSON.toJSONString(detailList));
                        orderShip.setUpdateTime(LocalDateTime.now());
                        iOrderShipService.updateById(orderShip);
                    }
                    result.addAll(detailList);
                } else {
                    result.addAll(JSON.parseArray(orderShip.getRouteInfo()));
                }
            }
        }
        return ResponseUtil.ok(result);
    }

    /**
     * 物流单列表
     *
     * @param uid
     * @return
     */
    @PostMapping("/ship/list")
    public Object shipList(@LoginUser Long uid, @RequestBody Order order) {
        List<OrderShip> orderShipList = iOrderShipService.list(new QueryWrapper<OrderShip>().eq("deleted", ConstantsEnum.DELETED_0.integerValue())
                .eq("order_no", order.getOrderNo()));
        List<String> collect = orderShipList.stream().map(x -> x.getShipSn()).distinct().collect(Collectors.toList());
        return ResponseUtil.ok(collect);
    }
}
