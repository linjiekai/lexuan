package com.zhuanbo.admin.api.controller;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.order.IncomeExpenseDTO;
import com.zhuanbo.admin.api.dto.order.OrderListDTO;
import com.zhuanbo.admin.api.dto.order.OrderParamsDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.dto.AdminShipsDTO;
import com.zhuanbo.core.dto.OrderRefundDto;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderDescribe;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.core.entity.OrderShip;
import com.zhuanbo.core.entity.Region;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.service.handler.IUserIncomeProcHandler;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IOrderDescribeService;
import com.zhuanbo.service.service.IOrderGoodsService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IOrderShipService;
import com.zhuanbo.service.service.IRegionService;
import com.zhuanbo.service.service.ISupplierService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.utils.LogOperateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {

    private final static DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderGoodsService orderGoodsService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IOrderDescribeService iShopOrderDescribeService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private IRegionService iRegionService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private ISupplierService iSupplierService;
    @Autowired
    private IOrderShipService iOrderShipService;
    @Autowired
    private IUserIncomeProcHandler iUserIncomeProcHandler;
    
    
    /**
     * 列表
     *
     * @param page
     * @param limit
     * @param sort
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       OrderParamsDTO orderParamsDTO) {

        Page<Order> pageCond = new Page<>(page, limit);

        Map<String, Object> map = new HashMap<>();
        if (orderParamsDTO != null) {
            if (StringUtils.isNotBlank(orderParamsDTO.getOrderNo())) {
                map.put("order_no", "%" + orderParamsDTO.getOrderNo() + "%");
            }
            if (StringUtils.isNotBlank(orderParamsDTO.getOrderStatus())) {
                map.put("order_status", orderParamsDTO.getOrderStatus());
            }
            if (StringUtils.isNotBlank(orderParamsDTO.getSupplierCode())) {
                map.put("supplier_code", orderParamsDTO.getSupplierCode());
            }
            Optional.ofNullable(orderParamsDTO.getUserId()).ifPresent(x -> map.put("user_id", x));
            Optional.ofNullable(orderParamsDTO.getGoodsId()).ifPresent(x -> map.put("goods_id", "%" + x + "%"));
            Optional.ofNullable(StringUtils.stripToNull(orderParamsDTO.getStartDate())).ifPresent(x -> map.put("startDate", x));
            Optional.ofNullable(StringUtils.stripToNull(orderParamsDTO.getEndDate())).ifPresent(x -> map.put("endDate", x));
            Optional.ofNullable(orderParamsDTO.getInviteUserId()).ifPresent(x -> map.put("inviteUserId", x));
            Optional.ofNullable(StringUtils.stripToNull(orderParamsDTO.getMobile())).ifPresent(x -> map.put("mobile", x));
        }
        IPage<Order> adIPage = orderService.orderList(pageCond, map);

        Map<String, Object> data = new HashMap<>();

        List<OrderListDTO> list = new ArrayList<>();
        List<OrderListDTO.OrderGoods> ogList = null;
        OrderListDTO.OrderGoods orderGoods1 = null;
        OrderListDTO.OrderListDTOBuilder builder;
        Region region = null;
        OrderListDTO orderListDTO;
        User user;
        Admin admin = null;

        for (Order o : adIPage.getRecords()) {
            // 商品列表
            List<OrderGoods> orderGoodsList = orderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_no", o.getOrderNo()));
            // 用户信息
            user = userService.getById(o.getUserId());
            // 副表信息
            OrderDescribe orderDescribe = iShopOrderDescribeService.getOne(new QueryWrapper<OrderDescribe>().eq("order_no", o.getOrderNo()));
            if (orderDescribe != null) {
                admin = iAdminService.getById(orderDescribe.getAdminId());// 操作人
            }
            builder = OrderListDTO.builder();

            builder = builder.addTime(o.getAddTime()).deliveryNo(orderDescribe.getShipSn()).invitePid(o.getInviteUserId())
                    .deliveryTime(StringUtils.isBlank(orderDescribe.getShipTime()) ? null : LocalDateTime.parse(orderDescribe.getShipTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .orderNo(o.getOrderNo()).payNo(o.getPayNo()).payTime(o.getPayDate() + " " + o.getPayTime()).price(o.getPrice())
                    .orderStatus(o.getOrderStatus());
            if (user != null) {
                builder = builder.userId(o.getUserId());
            }
            if (admin != null) {
                builder = builder.adminName(admin.getUsername());
            }
            if (orderDescribe != null) {
                if (orderDescribe.getProvinceId() != null) {
                    region = iRegionService.getById(orderDescribe.getProvinceId());
                    if (region != null) {
                        builder = builder.province(region.getName());
                    }
                }
                if (orderDescribe.getCityId() != null) {
                    region = iRegionService.getById(orderDescribe.getCityId());
                    if (region != null) {
                        builder = builder.city(region.getName());
                    }
                }
                if (orderDescribe.getAreaId() != null) {
                    region = iRegionService.getById(orderDescribe.getAreaId());
                    if (region != null) {
                        builder = builder.area(region.getName());
                    }
                }
                if (orderDescribe.getCountryId() != null) {
                    region = iRegionService.getById(orderDescribe.getCountryId());
                    if (region != null) {
                        builder = builder.country(region.getName());
                    }
                }
            }
            builder = builder.address(orderDescribe.getAddress())
                    .username(orderDescribe.getContactsName())
                    .mobile(orderDescribe.getMobile())
                    .remark(orderDescribe.getRemark());
            ogList = new ArrayList<>();
            int number = 0;
            List<String> supplierCodes = new ArrayList<>();// 供应商编号
            List<Integer> traceTypeList = new ArrayList<>();
            for (OrderGoods orderGoods : orderGoodsList) {
                Goods good = iGoodsService.getById(orderGoods.getGoodsId());
                orderGoods1 = OrderListDTO.OrderGoods.builder().goodsId(orderGoods.getGoodsId()).goodsName(orderGoods.getGoodsName())
                        .specifications(orderGoods.getSpecifications()).build();
                number += orderGoods.getNumber();
                ogList.add(orderGoods1);
            }
            builder.supplierList(iSupplierService.getNamesByCodes(supplierCodes)).traceTypeList(traceTypeList);

            // 物流信息
            List<OrderShip> shipList = iOrderShipService.list(new QueryWrapper<OrderShip>().select("order_no, ship_channel, ship_sn")
                    .eq("order_no", o.getOrderNo()).eq("deleted", ConstantsEnum.DELETED_0.integerValue()));

            List<OrderListDTO.Ships> shipList2 = shipList.stream().map(x -> OrderListDTO.Ships.builder()
                    .orderNo(x.getOrderNo())
                    .shipChannel(x.getShipChannel())
                    .shipSn(x.getShipSn())
                    .build()).collect(Collectors.toList());
            shipList2 = shipList2 == null ? new ArrayList<>() : shipList2;
            builder.shipList(shipList2);

            orderListDTO = builder.orderGoods(ogList).build();
            orderListDTO.setNumber(number);// 商品数量

            list.add(orderListDTO);
        }
        data.put("total", adIPage.getTotal());
        data.put("items", list);
        return ResponseUtil.ok(data);
    }

    /**
     * 发货
     *
     * @param adminId
     * @param jsonObject
     * @return
     */
    @Transactional
    @PostMapping("/ship")
    public Object ship(@LoginAdmin Integer adminId, @RequestBody(required = false) JSONObject jsonObject) {
        LogOperateUtil.log("订单管理", "发货", String.valueOf(jsonObject.getString("orderNo")), adminId.longValue(), 0);
        String orderNo = jsonObject.getString("orderNo");
        String shipChannel = jsonObject.getString("shipChannel");
        String shipSn = jsonObject.getString("shipSn");

        if (jsonObject == null) {
            return ResponseUtil.fail("11111", "参数不能为空");
        }
        if (StringUtils.isBlank(orderNo)) {
            return ResponseUtil.fail("11111", "缺少参数：orderNo");
        }
        if (StringUtils.isBlank(shipChannel)) {
            return ResponseUtil.fail("11111", "缺少参数：shipChannel");
        }
        if (StringUtils.isBlank(shipSn)) {
            return ResponseUtil.fail("11111", "缺少参数：shipSn");
        }

        String s = orderNo + "|" + shipChannel + "|" + shipSn;
        orderService.batchShip(Arrays.asList(s), adminId);

        OrderParamsDTO orderParamsDTO = new OrderParamsDTO();
        orderParamsDTO.setOrderNo(orderNo);
        return ResponseUtil.ok(list(adminId, 1, 1, null, orderParamsDTO));
    }

    @Transactional
    @PostMapping("/ships")
    public Object ships(@LoginAdmin Integer adminId, @RequestBody(required = false) AdminShipsDTO adminShipsDTO) {

        LogOperateUtil.log("订单管理", "发货", adminShipsDTO.getList().get(0).getOrderNo(), adminId.longValue(), 0);

        List<String> stringList = new ArrayList<>();
        StringBuffer stringBuffer;

        for (OrderShip o : adminShipsDTO.getList()) {
            if (StringUtils.isBlank(o.getOrderNo()) || StringUtils.isBlank(o.getShipChannel()) || StringUtils.isBlank(o.getShipSn()) ) {
                return ResponseUtil.fail("11111", "缺少参数：orderNo 或 shipChannel 或 shipSn");
            }
            stringBuffer = new StringBuffer();
            stringBuffer.append(o.getOrderNo()).append("|").append(o.getShipChannel()).append("|").append(o.getShipSn());
            stringList.add(stringBuffer.toString());
        }

        orderService.batchShip(stringList, adminId);

        OrderParamsDTO orderParamsDTO = new OrderParamsDTO();
        orderParamsDTO.setOrderNo(adminShipsDTO.getList().get(0).getOrderNo());
        return ResponseUtil.ok(list(adminId, 1, 1, null, orderParamsDTO));
    }

    @GetMapping("/incomeExpense")
    public Object incomeExpense(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer limit,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @Sort @RequestParam(defaultValue = "add_time") String sort,
                                OrderParamsDTO orderParamsDTO) {

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                simpleDateFormat.parse(startDate);
                simpleDateFormat.parse(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return ResponseUtil.fail("11111", "时间格式不正确");
            }
            queryWrapper.between("pay_date", startDate, endDate);
        }
        if (orderParamsDTO != null) {
            if (StringUtils.isNotBlank(orderParamsDTO.getPayNo())) {
                queryWrapper.like("pay_no", orderParamsDTO.getPayNo());
            }
            if (orderParamsDTO.getUserId() != null) {
                queryWrapper.like("user_id", orderParamsDTO.getUserId());
            }
        }

        queryWrapper.orderByDesc("pay_date");
        queryWrapper.orderByDesc("pay_time");
        // 状态：S WS WD
        queryWrapper.in("order_status", Arrays.asList(OrderStatus.SUCCESS.getId(),
                OrderStatus.WAIT_SHIP.getId(), OrderStatus.WAIT_DELIVER.getId()));

        Page<Order> pageCond = new Page<>(page, limit);
        IPage<Order> iPage = orderService.page(pageCond, queryWrapper);

        List<IncomeExpenseDTO> list = new ArrayList<>();
        if (iPage.getRecords().size() > 0) {

            List<Order> orderList = iPage.getRecords();

            User user = null;
            String AliEe = "支付宝商户号";
            String WXEe = "微信支付号";

            String aliCode = "ALIPAY";
            String wxCode = "WEIXIN";
            String xfhlCode = "XFHL";
            IncomeExpenseDTO.IncomeExpenseDTOBuilder builder = null;

            for (Order o : orderList) {
                user = userService.getById(o.getUserId());
                if (user == null) {
                    return ResponseUtil.fail("11111", "用户信息不存在");
                }
                builder = IncomeExpenseDTO.builder().payNo(o.getPayNo()).payStatus(o.getOrderStatus())
                        .payTime(o.getPayDate() + "-" + o.getPayTime()).payType(o.getBankCode()).userId(user.getId()).price(o.getPrice()).userName(user.getNickname());
                if (aliCode.equals(o.getBankCode())) {
                    builder = builder.payType("支付宝支付").payEe(AliEe);
                } else if (wxCode.equals(o.getBankCode())) {
                    builder = builder.payType("微信支付").payEe(WXEe);
                } else if (xfhlCode.equals(o.getBankCode())) {
                    builder = builder.payType("幸福狐狸");
                } else {
                    builder = builder.payType("未知");
                }
                list.add(builder.build());
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());
        data.put("items", list);
        return ResponseUtil.ok(data);
    }

    /**
     * 上传excel批量发货
     * @param adminId
     * @param file excel表格，格式[orderNo][shipChannel][shipSn]，即 [订单号][物流公司号][物流号]
     * @return
     */
    @PostMapping("/batchShip")
    public Object shipBatchUpload(@LoginAdmin Integer adminId, @RequestParam("file") MultipartFile file) {
        LogOperateUtil.log("订单管理", "批量发货", "", adminId.longValue(), 0);

        try {

            List<String> batchMsg = new ArrayList<>();

            Workbook workbook;
            if (file.getOriginalFilename().endsWith(".xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else {
                workbook = new XSSFWorkbook(file.getInputStream());
            }
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();// 每一页
            StringBuffer stringBuffer;
            while (sheetIterator.hasNext()) {
                Sheet sheetNext = sheetIterator.next();
                Iterator<Row> rowIterator = sheetNext.rowIterator();// 每一行
                boolean firstRow = true;
                while (rowIterator.hasNext()) {
                    Row rowNext = rowIterator.next();
                    if (firstRow) {
                        firstRow = !firstRow;
                        continue;
                    }
                    Iterator<Cell> cellIterator = rowNext.cellIterator();// 每一列
                    int i = 0;
                    stringBuffer = new StringBuffer();
                    while (cellIterator.hasNext()) {
                        if (i > 2) {
                            break;
                        }
                        String s = dataFormatter.formatCellValue(cellIterator.next());
                        if (StringUtils.isBlank(s)) {
                            break;
                        }
                        stringBuffer.append(s);
                        if (i < 2) {
                            stringBuffer.append("|");
                        }
                        i++;
                    }
                    Optional.ofNullable(StringUtils.trimToNull(stringBuffer.toString())).ifPresent(x -> batchMsg.add(x));
                }
            }
            orderService.batchShip(batchMsg, adminId);
        } catch (ShopException e) {
            log.error("上传批量发货异常：{}", e);
            throw e;
        } catch (Exception e) {
            log.error("上传批量发货异常：{}", e);
            return ResponseUtil.fail();
        }
        return ResponseUtil.ok();
    }

    /**
     * 订单备注编辑
     */
    @PostMapping("/edit/remark")
    public Object editRemark(@LoginAdmin Integer adminId, @RequestBody JSONObject reqMsg){
        LogOperateUtil.log("订单管理", "备注信息修改", "", adminId.longValue(), 0);
        String orderNo = reqMsg.getString("orderNo");
        String remark = reqMsg.getString("remark");
        OrderDescribe orderDescribe = iShopOrderDescribeService.getOne(new QueryWrapper<OrderDescribe>().eq("order_no", orderNo));
        if (orderDescribe == null) {
            log.error("|订单管理|备注信息修改|订单编号无效");
            throw new ShopException(20001);
        }
        orderDescribe.setRemark(remark);
        iShopOrderDescribeService.updateById(orderDescribe);
        return ResponseUtil.ok();
    }

    /**
     * 查
     * @param id
     * @return
     *//*
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return ResponseUtil.badResult();
        } else {

            User user = userService.getById(order.getUserId());
            List<OrderGoods> orderGoodsList = orderGoodsService.findByOrderId(id);
            OrderDTO orderDTO = new OrderDTO(order, user, orderGoodsList);
            return ResponseUtil.ok(orderDTO);
        }
    }

    *//**
     * 订单状态修改
     * @param status 状态
     * @param id 订单id
     * @param version 锁版本
     * @return
     *//*
    @PostMapping("/status/{id}/{version}/{status}")
    public Object status(@LoginAdmin Integer adminId,
                         @PathVariable("id") Integer id,
                         @PathVariable("version") Integer version,
                         @PathVariable("status") String status) {

        QueryWrapper<Order> queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id);
        queryWrapper.eq("version", version);

        Order order = orderService.getOne(queryWrapper);
        if (order == null) {
            return ResponseUtil.fail(404, "数据已被修改或不存在，请重新载入数据后再操作");
        }
        order.setOrderStatus(status);
        return orderService.updateById(order) ? ResponseUtil.ok() : ResponseUtil.fail(502, "更新失败，请重新载入数据后再操作");
    }

    *//**
     * 订单删除
     * @param adminId
     * @param id
     * @return
     *//*
    @PostMapping("/delete/{id}")
    public Object delete(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return  ResponseUtil.badResult();
        }
        order.setOrderStatus("c");
        orderService.updateById(order);
        return ResponseUtil.ok();
    }*/

    /**
     * 退款
     * @param adminId
     * @return
     * @throws Exception
     */
    @PostMapping("/refund")
    public Object refund(@LoginAdmin Integer adminId, @RequestBody JSONObject params) throws Exception {
        log.info("|订单退款|接收到请求报文:params={}", params);
        String orderNo = params.getString("orderNo");
        // LogOperateUtil.log("订单管理", "订单退款", orderNo, adminId.longValue(), 0);

        if(StringUtils.isBlank(orderNo)){
            return ResponseUtil.fail(10402, "缺少订单号");
        }
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no",orderNo));
        if(null == order){
            return ResponseUtil.fail(10402, "订单不存在");
        }
        String orderStatus = order.getOrderStatus();
        if (!(orderStatus.equals(OrderStatus.WAIT_SHIP.getId())
                || orderStatus.equals(OrderStatus.WAIT_DELIVER.getId())
                || orderStatus.equals(OrderStatus.SUCCESS.getId()))) {
            return ResponseUtil.fail(10402, "订单状态不支持退款");
        }
        Integer purchType = order.getPurchType();

        /*if(PurchType.BUY.getId() != purchType && PurchType.ONLINE.getId() != purchType && PurchType.TAKE.getId() != purchType){
            return ResponseUtil.fail(10402, "支持退款订单类型："+PurchType.BUY.getName()+";"+PurchType.ONLINE.getName()+";"+PurchType.TAKE.getName());
        }*/

        OrderRefundDto refundDto = new OrderRefundDto();
        refundDto.setAdminId(adminId);
        refundDto.setOperator(iAdminService.getAdminName(adminId));
        refundDto.setOrderNo(orderNo);
        iUserIncomeProcHandler.orderRefundProc(refundDto);

        return ResponseUtil.ok();
    }
    
    @PostMapping("/buyTypeCount")
    public Object buyTypeCount(@RequestBody OrderParamsDTO orderParamsDTO) {

    	Integer buyTypeCount = orderService.count(new QueryWrapper<Order>()
        		.eq("user_id", orderParamsDTO.getUserId())
        		.notIn("order_status", "W", "C")
        		);
    	
        Map<String, Object> result = new HashMap<>();
        result.put("buyTypeCount", buyTypeCount);
        return ResponseUtil.ok(result);
    }

    /**
     * 订单详情
     *
     * @param body 订单信息
     * @return 订单操作结果
     * 成功则
     */
    @PostMapping("/detail")
    public Object detail(@RequestBody String body) {

        String orderNo = JacksonUtil.parseString(body, "orderNo");
        // 订单信息
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no",orderNo));
        OrderDescribe orderDescribe = iShopOrderDescribeService.getOne(new QueryWrapper<OrderDescribe>().eq("order_no",orderNo));
        if (order == null) {
            return ResponseUtil.result(50001);
        }
        
        Integer buyTypeCount = orderService.count(new QueryWrapper<Order>()
        		.eq("user_id", order.getUserId())
        		.notIn("order_status", "W", "C")
        		);
        
        Map<String, Object> orderVo = new HashMap<String, Object>();
        orderVo.put("id", order.getId());
        orderVo.put("orderNo", order.getOrderNo());
        orderVo.put("status",order.getOrderStatus());
        orderVo.put("orderStatusText", OrderStatus.parse(order.getOrderStatus()).getName());
        orderVo.put("name", orderDescribe.getContactsName());
        orderVo.put("mobile", orderDescribe.getMobile());
        orderVo.put("address", orderDescribe.getAddress());
        orderVo.put("totalPrice", order.getTotalPrice());
        orderVo.put("price", order.getPrice());
        orderVo.put("goodsTotalPrice",orderDescribe.getGoodsTotalPrice());
        orderVo.put("shipPrice",orderDescribe.getShipPrice());
        orderVo.put("addTime", order.getAddTime());
        orderVo.put("payNo", order.getPayNo());
        orderVo.put("payTime", order.getPayDate()+" "+order.getPayTime());
        orderVo.put("shipTime",orderDescribe.getShipTime());
        orderVo.put("userId", order.getUserId());
        orderVo.put("buyTypeCount", buyTypeCount);
        orderVo.put("buyType", order.getBuyType());
        orderVo.put("couponSn", orderDescribe.getCouponSn());
        
        
        long expTime;
        if (order.getExpTime() == null || order.getExpTime().equals(0L)) {
            LocalDateTime plus = order.getAddTime().plus(24, ChronoUnit.HOURS);
            expTime = plus.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            expTime = order.getExpTime();
        }

        LocalDateTime now = LocalDateTime.now();
        long expTimeNow = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();// 当前过了多久
        orderVo.put("expTime", expTimeNow < expTime ? (expTime -  expTimeNow) / 1000 : -1);

        List<OrderGoods> orderGoodsList = orderGoodsService.list(
                new QueryWrapper<OrderGoods>().eq("order_no",order.getOrderNo()));
        List<Map<String, Object>> orderGoodsVoList = new ArrayList<>();
        for (OrderGoods orderGoods : orderGoodsList) {
            Map<String, Object> orderGoodsVo = new HashMap<>();
            orderGoodsVo.put("goodsId", orderGoods.getGoodsId());
            orderGoodsVo.put("productId", orderGoods.getProductId());
            orderGoodsVo.put("goodsName", orderGoods.getGoodsName());
            orderGoodsVo.put("specifications",orderGoods.getSpecifications());
            orderGoodsVo.put("number", orderGoods.getNumber());
            orderGoodsVo.put("picUrl", orderGoods.getPicUrl());
            orderGoodsVo.put("price", orderGoods.getPrice());
            orderGoodsVoList.add(orderGoodsVo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderInfo", orderVo);
        result.put("orderGoods", orderGoodsVoList);
        return ResponseUtil.ok(result);

    }
}
