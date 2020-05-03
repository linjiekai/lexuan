package com.zhuanbo.admin.api.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhuanbo.admin.api.dto.withdrorder.WithdrOrderDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.annotation.UnAuthAnnotation;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.PlatformType;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.constants.WithdrOrderStatusEnum;
import com.zhuanbo.core.dto.AdminWithdrDTO;
import com.zhuanbo.core.dto.WithdrawOrderAuditDTO;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ReportExcel;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.ICipherService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.INotifyMsgService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service.IWithdrOrderService;
import com.zhuanbo.service.vo.WithdrOrderExportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @title: WithdrOrderController
 * @projectName mpmall.api
 * @description: 提现订单
 * @date 2019/10/22 14:01
 */
@Slf4j
@RestController
@RequestMapping("/admin/withdr/order")
public class WithdrOrderController {

    @Autowired
    private IWithdrOrderService withdrOrderService;
    @Autowired
    private IUserIncomeDetailsService userIncomeDetailsService;
    @Autowired
    private INotifyMsgService notifyMsgService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ICipherService cipherService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private IAdminService adminService;
    @Autowired
    private IWithdrOrderService iWithdrOrderService;
    @Autowired
    private ISeqIncrService iSeqIncrService;

    /**
     * 提现列表
     *
     * @param adminId
     * @param reqMsg
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestBody JSONObject reqMsg) {
        log.info("|提现订单|列表|接收到请求报文:{}", reqMsg);
        Long userId = reqMsg.getLong("userId");
        String nickname = reqMsg.getString("nickname");
        String orderStatus = reqMsg.getString("orderStatus");
        String startTime = reqMsg.getString("startTime");
        String endTime = reqMsg.getString("endTime");
        Long page = reqMsg.getLong("page");
        Long limit = reqMsg.getLong("limit");
        IPage<WithdrOrder> iPage = new Page<>(page, limit);
        QueryWrapper<WithdrOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("add_time");
        if (null != userId) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.isNotBlank(orderStatus)) {
            queryWrapper.eq("order_status", orderStatus);
        }
        if (StringUtils.isNotBlank(nickname)) {
            List<User> userList = userService.list(new QueryWrapper<User>().like("nickname", nickname).eq("deleted", 0));
            if (userList != null && userList.size() > 0) {
                List<Long> userIds = userList.stream().map(User::getId).collect(toList());
                queryWrapper.in("user_id", userIds);
            }
        }
        if (StringUtils.isNotBlank(startTime)) {
            queryWrapper.ge("add_time", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            queryWrapper.le("add_time", endTime);
        }
        IPage<WithdrOrder> pageResult = withdrOrderService.page(iPage, queryWrapper);
        long total = pageResult.getTotal();
        List<WithdrOrderDTO> orderDTOList = new ArrayList<>();
        List<WithdrOrder> records = pageResult.getRecords();
        if (records != null && records.size() > 0) {
            // 用户信息完善
            List<Long> userIds = records.stream().map(WithdrOrder::getUserId).collect(toList());
            List<User> userList = userService.list(new QueryWrapper<User>().in("id", userIds).eq("deleted", 0));
            // 银行信息完善
            List<String> bankCodes = records.stream().map(WithdrOrder::getBankCode).distinct().collect(toList());
            List<Dictionary> banks = dictionaryService.list(new QueryWrapper<Dictionary>()
                    .eq("category", "bankCode").in("str_val", bankCodes));
            for (int i = 0; i < records.size(); i++) {
                WithdrOrder withdrOrder = records.get(i);
                WithdrOrderDTO withdrOrderDTO = new WithdrOrderDTO();
                BeanUtils.copyProperties(withdrOrder, withdrOrderDTO);
                if (userList != null && userList.size() > 0) {
                    userList.forEach(user -> {
                        if (withdrOrder.getUserId().equals(user.getId())) {
                            withdrOrderDTO.setUserName(user.getUserName());
                            withdrOrderDTO.setNickname(user.getNickname());
                        }
                    });
                }
                String bankCode = withdrOrderDTO.getBankCode();
                if (StringUtils.isNotBlank(bankCode)) {
                    if (banks != null && banks.size() > 0) {
                        banks.forEach(bank -> {
                            if (bankCode.equals(bank.getStrVal())) {
                                withdrOrderDTO.setBankName(bank.getName());
                            }
                        });
                    }
                }
                String bankCardNo = withdrOrderDTO.getBankCardNo();
                if (StringUtils.isNotBlank(bankCardNo)) {
                    try {
                        bankCardNo = cipherService.decryptAES(bankCardNo);
                    } catch (Exception e) {
                        log.error("|提现列表|银行卡解密失败|银行卡号:{}|", bankCardNo);
                    }
                    //bankCardNo = bankCardNo.substring(0, bankCardNo.length() - 8) + "****" + bankCardNo.substring(bankCardNo.length() - 4);
                    withdrOrderDTO.setBankCardNo(bankCardNo);
                }
                orderDTOList.add(withdrOrderDTO);
            }
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("total", total);
        dataMap.put("items", orderDTOList);
        return ResponseUtil.ok(dataMap);
    }

    /**
     * 提现审核
     *
     * @param adminId
     * @param reqMsg
     * @return
     */
    @Transactional
    @PostMapping("/audit")
    public Object audit(@LoginAdmin Integer adminId, @RequestBody JSONObject reqMsg) throws Exception {
        log.info("|提现订单|提现审核|接收到请求报文:{}", reqMsg);
        String orderNo = reqMsg.getString("orderNo");
        String orderStatus = reqMsg.getString("orderStatus");
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("order_no", orderNo));
        if (withdrOrder == null) {
            log.error("|提现订单|提现审核|提现订单号无效,订单号:{}|", orderNo);
            throw new ShopException(20001);
        }
        if (!WithdrOrderStatusEnum.AUDIT_WAIT.getId().equals(withdrOrder.getOrderStatus())) {
            log.error("|提现订单|提现审核|订单原状态无效,原订单状态:{}|", withdrOrder.getOrderStatus());
            throw new ShopException(20002);
        }
        // 获取审核人信息
        Admin admin = adminService.getOne(new QueryWrapper<Admin>().eq("id", adminId).eq("deleted", 0));

        LocalDateTime now = LocalDateTime.now();
        // 更改审核状态
        withdrOrder.setOrderStatus(orderStatus);
        withdrOrder.setAuditorId(adminId);
        withdrOrder.setAuditor(admin.getUsername());
        withdrOrder.setAuditTime(now);
        withdrOrderService.updateById(withdrOrder);

        Map<String, Object> params = new HashMap<>();
        log.info("|提现审核|审核审核|调用mppay系统提现审核|订单编号:{}|", orderNo);
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_WITHDR_AUDIT.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("clientIp", withdrOrder.getClientIp());
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), withdrOrder.getPlatform());
        params.put("orderNo", withdrOrder.getOrderNo());
        params.put("orderStatus", orderStatus);
        params.put("sysCnl", withdrOrder.getSysCnl());

        //同步提现审核状态到pay域
        JSONObject unbind = requestResult(params);
        if (unbind == null) {
            log.error("|提现订单|提现审核失败|提现的用户无效,orderNo:{}|", withdrOrder.getOrderNo());
            return ResponseUtil.result(51002);
        }

        // 如果未通过,审核结果通知:[你的提现申请未能成功，如果疑问，请联系客服解决]
        if (WithdrOrderStatusEnum.REJECT.getId().equals(orderStatus)) {
            log.info("|提现审核|审核拒绝|发起结果通知|订单编号:{}|", orderNo);
            Long userId = withdrOrder.getUserId();
            User user = userService.getById(userId);
            if (user == null) {
                log.error("|提现订单|提现审核|提现的用户无效,用户id:{}|", userId);
                throw new ShopException(10007);
            }
            notifyMsgService.notifyAndPush(user, ConstantsEnum.PLATFORM_ZBMALL.stringValue(),
                    1, "系统通知", "你的提现申请未能成功，如果疑问，请联系客服解决。", MapUtil.of("type", 3, "link", ""));
        }

        return ResponseUtil.ok();
    }

    /**
     * 按照时间 - 批量取消提现
     *
     * @return
     */
    @GetMapping("/batch/audit")
    public void batchAuditByAddTime() throws Exception {
        if (1 == 1) {
            // 2019-12-21 23:48:55
            return;
        }
        log.info("|批量取消提现|");
        List<WithdrOrder> withdrOrderList = withdrOrderService.list(new QueryWrapper<WithdrOrder>().ge("add_time", "2019-12-10 00:00:00").le("add_time", "2019-12-20 14:41:00").eq("order_status", "A"));

        withdrOrderList.forEach(w -> {
            JSONObject jsonStr = new JSONObject();
            jsonStr.put("orderNo", w.getOrderNo());
            jsonStr.put("orderStatus", WithdrOrderStatusEnum.REJECT.getId());
            try {
                audit(1, jsonStr);
            } catch (Exception e) {
                log.info("|批量取消提现|订单No:{}, 失败:{}", w.getOrderNo(), e.toString());
                e.printStackTrace();
            }
        });
    }

    /**
     * 调用mppay系统统一入口
     *
     * @param params
     * @return
     * @throws Exception
     */
    private JSONObject requestResult(Map<String, Object> params) throws Exception {

        log.info("请求pay参数：{}", JacksonUtil.objTojson(params));
        String plain = Sign.getPlain(params);
        plain += "&key=" + authConfig.getMercPrivateKey();
        log.info(plain);
        String sign = Sign.sign(plain);
        log.info(sign);
        log.info("key:{}", authConfig.getMercPrivateKey());
        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
        String s = HttpUtil.sendPostJson(authConfig.getPayUrl(), params, headers);
        log.info("请求pay 结果：{}", s);
        if (StringUtils.isBlank(s)) {
            throw new ShopException(10502);
        }

        JSONObject json = JSONObject.parseObject(s);
        if (!ReqResEnum.C_10000.String().equalsIgnoreCase(json.getString(ReqResEnum.CODE.String()))) {
            log.error("请求接口失败,params[{}],response[{}]", JacksonUtil.objTojson(params), json);
            String code = json.get("code").toString();
            String msg = json.get("msg").toString();
            throw new ShopException(code, msg);
        }
        return json.getJSONObject(ReqResEnum.DATA.String());
    }


    /**
     * 批量审核
     * @param adminId
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/batchAudit")
    public Object batchAudit(@LoginAdmin Integer adminId, @RequestBody JSONObject params) throws Exception {
        log.info("====批量提现审核，params={}",params.toString());
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");
        Long userId = params.getLong("userId");
        String nickname = params.getString("nickname");
        String orderStatus = params.getString("orderStatus");

        if(StringUtils.isBlank(orderStatus) || !(WithdrOrderStatusEnum.REJECT.getId().equals(orderStatus) || WithdrOrderStatusEnum.WAIT.getId().equals(orderStatus))){
            return ResponseUtil.badArgumentValue();//状态非法
        }

        QueryWrapper<WithdrOrder> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(StringUtils.stripToNull(startDate)).ifPresent(s -> queryWrapper.ge("add_time",s));
        Optional.ofNullable(StringUtils.stripToNull(endDate)).ifPresent(s -> queryWrapper.le("add_time",s));
        Optional.ofNullable(userId).ifPresent(s -> queryWrapper.eq("user_id",s));
        Optional.ofNullable(StringUtils.stripToNull(nickname)).ifPresent(s -> {
            List<User> userList = userService.list(new QueryWrapper<User>().like("nickname", s).eq("deleted", 0));
            if (CollectionUtils.isNotEmpty(userList)) {
                List<Long> userIds = userList.stream().map(User::getId).collect(toList());
                queryWrapper.in("user_id", userIds);
            }
        });

        queryWrapper.eq("order_status", WithdrOrderStatusEnum.AUDIT_WAIT.getId());//待审核
        List<WithdrOrder> resultList = withdrOrderService.list(queryWrapper);

        JSONArray failOrderNo = new JSONArray();
        if(CollectionUtils.isNotEmpty(resultList)){
            resultList.forEach(w -> {
                JSONObject jsonStr = new JSONObject();
                jsonStr.put("orderNo", w.getOrderNo());
                jsonStr.put("orderStatus", orderStatus);
                try {
                    audit(adminId, jsonStr);
                } catch (Exception e) {
                    failOrderNo.add(w.getOrderNo());
                    log.error("|批量提现审核|订单No:{}, 失败:{}", w.getOrderNo(), e.toString());
                    e.printStackTrace();
                }
            });

        }

        if(CollectionUtils.isEmpty(failOrderNo)){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.fail(11111, "处理异常",failOrderNo);
        }

    }

    @PostMapping("/applyByCoder")
    @UnAuthAnnotation
    public Object applyByCoder(@RequestBody JSONArray jsonArray) {
        log.info("applyByCoder:: 开始");
        List< cn.hutool.json.JSONObject> list = CollUtil.newArrayList();
        for (int i = 0; i <jsonArray.size() ; i++) {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jsonArray.get(i));
            LocalDateTime now = LocalDateTime.now();
            Long userId =  Long.valueOf(jsonObject.get("userId")+"");
            BigDecimal price = new BigDecimal(jsonObject.get("price")+"");
            String bankCode =  (String)jsonObject.get("bankCode");
            String bankCardNo =  (String)jsonObject.get("bankCardNo");
            String bankCardType =  (String)jsonObject.get("bankCardType");
            String clientIp = (String)jsonObject.get("clientIp");
            String argNo = (String)jsonObject.get("argNo");
            String sysCnl = (String)jsonObject.get("sysCnl");
            String tradeType = (String)jsonObject.get("tradeType");
            String orderNo = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + iSeqIncrService.nextVal("withdr_order_no", 8, Align.LEFT);
            Map<String, Object> params = new HashMap<>();
            log.info("|applyOnce|提现申请|调用mppay系统提现|订单编号:{}|", orderNo);
            params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_WITHDR_APPLY.String());
            params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
            params.put("orderNo", orderNo);
            params.put("orderDate", DateUtil.toyyyy_MM_dd(now));
            params.put("orderTime", DateUtil.toHH_mm_ss(now));
            params.put("agrNo", argNo);
            params.put("price", price);
            params.put("userId", userId);
            params.put("bankCode", bankCode);
            params.put("checkName", "NO_CHECK");
            params.put("bankCardNo", bankCardNo);
            params.put("clientIp", clientIp);
            params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
            params.put(ReqResEnum.PLATFORM.String(), "XFYLMALL");
            params.put("tradeType", tradeType);
            params.put("bankCardType", bankCardType);
            params.put("sysCnl", sysCnl);

            JSONObject unbind = null;
            String exCode = null;
            String exMsg = null;
            try {
                unbind = requestResult(params);
            } catch (ShopException se) {
                log.error("|applyByCoder|提现申请|调用mppay失败,code:{},msg:{}", exCode, exMsg);
                list.add(jsonObject);
                continue;
            } catch (Exception e) {
                log.error("|applyByCoder|提现申请|调用mppay异常:{}", e.getMessage());
                list.add(jsonObject);
                continue;
            }

            // 更新银行及银行卡信息
            String bindBankCode = unbind.getString("bankCode");
            String bindBankCardNo = unbind.getString("bankCardNo");
            String bindBankCardName = unbind.getString("bankCardName");

            WithdrOrder withdrOrder = new WithdrOrder();
            withdrOrder.setUserId(userId);
            withdrOrder.setMercId(PlatformType.ZBMALL.getId());
            withdrOrder.setPlatform(PlatformType.ZBMALL.getCode());
            withdrOrder.setOrderNo(orderNo);
            withdrOrder.setOrderDate(DateUtil.toyyyy_MM_dd(now));
            withdrOrder.setOrderTime(DateUtil.toHH_mm_ss(now));
            withdrOrder.setOrderStatus(WithdrOrderStatusEnum.AUDIT_WAIT.getId());
            withdrOrder.setPrice(price);
            withdrOrder.setBankCode(bankCode);
            withdrOrder.setBankCardNo(bankCardNo);
            withdrOrder.setBankCardType(bankCardType);
            withdrOrder.setCheckName("NO_CHECK");
            withdrOrder.setClientIp(clientIp);
            withdrOrder.setOutAgrNo(argNo);
            withdrOrder.setSysCnl(sysCnl);
            withdrOrder.setTradeType(tradeType);
            withdrOrder.setAddTime(now);
            withdrOrder.setUpdateTime(now);
            if (StringUtils.isNotBlank(bindBankCode)) {
                withdrOrder.setBankCode(bindBankCode);
            }
            if (StringUtils.isNotBlank(bindBankCardNo)) {
                withdrOrder.setBankCardNo(bindBankCardNo);
            }
            withdrOrder.setBankCardName(bindBankCardName);
            iWithdrOrderService.save(withdrOrder);
            log.info("|applyByCoder|提现申请|成功，userid:{} ", userId);
        }
        log.info("|applyByCoder|提现申请|失败记录，list:{} ", JacksonUtil.objTojson(list));
        return ResponseUtil.ok(list);
    }

    /**
     * 批量审核
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/batch/audit/by/orderno")
    public Object batchAuditByOrderNo(@RequestBody JSONObject params) throws Exception {
        log.info("====批量提现审核,根据提现订单id，params={}",params.toString());
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");
        Long userId = params.getLong("userId");
        String nickname = params.getString("nickname");
        JSONArray orderNoArr = params.getJSONArray("orderNo");
        JSONArray orderIdArr = params.getJSONArray("orderId");
        String orderStatus = params.getString("orderStatus");
        String adminId = params.getString("adminId");

        if(StringUtils.isBlank(orderStatus) || !(WithdrOrderStatusEnum.REJECT.getId().equals(orderStatus) || WithdrOrderStatusEnum.WAIT.getId().equals(orderStatus))){
            return ResponseUtil.badArgumentValue();//状态非法
        }

        QueryWrapper<WithdrOrder> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(StringUtils.stripToNull(startDate)).ifPresent(s -> queryWrapper.ge("add_time",s));
        Optional.ofNullable(StringUtils.stripToNull(endDate)).ifPresent(s -> queryWrapper.le("add_time",s));
        Optional.ofNullable(userId).ifPresent(s -> queryWrapper.eq("user_id",s));
        Optional.ofNullable(StringUtils.stripToNull(nickname)).ifPresent(s -> {
            List<User> userList = userService.list(new QueryWrapper<User>().like("nickname", s).eq("deleted", 0));
            if (CollectionUtils.isNotEmpty(userList)) {
                List<Long> userIds = userList.stream().map(User::getId).collect(toList());
                queryWrapper.in("user_id", userIds);
            }
        });
        if (orderNoArr != null && orderNoArr.size() > 0) {
            queryWrapper.in("order_no", orderNoArr);
        }
        if (orderIdArr != null && orderIdArr.size() > 0) {
            queryWrapper.in("id", orderIdArr);
        }

        queryWrapper.eq("order_status", WithdrOrderStatusEnum.AUDIT_WAIT.getId());//待审核
        List<WithdrOrder> resultList = withdrOrderService.list(queryWrapper);

        JSONArray failOrderNo = new JSONArray();
        if(CollectionUtils.isNotEmpty(resultList)){
            resultList.forEach(w -> {
                JSONObject jsonStr = new JSONObject();
                jsonStr.put("orderNo", w.getOrderNo());
                jsonStr.put("orderStatus", orderStatus);
                try {
                    audit(Integer.valueOf(adminId), jsonStr);
                } catch (Exception e) {
                    failOrderNo.add(w.getOrderNo());
                    log.error("|批量提现审核|订单No:{}, 失败:{}", w.getOrderNo(), e.toString());
                    e.printStackTrace();
                }
            });

        }
        if(CollectionUtils.isEmpty(failOrderNo)){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.fail(11111, "处理异常",failOrderNo);
        }
    }

    /**
     * 上传审核
     * @param adminId
     * @param key
     * @param batchList
     * @param orderStatus
     * @return
     */
    private Map<String, Object> uploadAudit(Integer adminId, String key, List<WithdrawOrderAuditDTO> batchList, String orderStatus) {
        log.info("====上传批量提现审核,开始");
        Map<String, Object> result = new HashMap<>();
        if(CollectionUtils.isNotEmpty(batchList)){
            // 当前进度
            int doIndex = 0;
            int allSize = batchList.size();
            for(WithdrawOrderAuditDTO orderAuditDTO : batchList) {
                doIndex++;
                RedisUtil.set(key, new BigDecimal(doIndex + "").divide(new BigDecimal(allSize + ""), 2, RoundingMode.HALF_UP).toString(), 120);
                JSONObject jsonStr = new JSONObject();
                jsonStr.put("orderNo", orderAuditDTO.getOrderNo());
                jsonStr.put("orderStatus", orderStatus);
                Object auditRet = null;
                try {
                    auditRet = audit(adminId, jsonStr);
                    orderAuditDTO.setAuditMsg(JSONUtil.parseObj(auditRet).getStr("msg"));
                } catch (ShopException e) {
                    orderAuditDTO.setAuditMsg(e.getMsg());
                } catch (Exception e) {
                    orderAuditDTO.setAuditMsg("处理异常");
                }
            }
            // 过滤保留异常单
            List<WithdrawOrderAuditDTO> errorList;
            errorList = batchList.stream().filter(w -> !"成功".equals(w.getAuditMsg())).collect(toList());
            if (errorList.size() > 0){
                result.put("extraCode", "-1");
                Map<String, Object> dmap = new HashMap<>();
                dmap.put("cnheader", Lists.newArrayList("订单号", "原因"));
                dmap.put("enheader", Lists.newArrayList("orderNo", "auditMsg"));
                dmap.put("list", errorList);
                result.put("extraData", dmap);
                result.put("progress", "1.00");
                RedisUtil.set(key, result, 120);
            }
        } else {
            result.put("progress", "1.00");
        }

        log.info("====上传批量提现审核,结束");
        return result;
    }
    /**
     * 上传审核
     * @param adminId
     * @param key
     * @param orderStatus
     * @return
     */
    @GetMapping("/upload/progress")
    public Object auditProgress(@LoginAdmin Integer adminId, String key, String orderStatus) {
        Map<String, Object> result = new HashMap<>();
        Object progressValue = RedisUtil.get(key);
        if (progressValue != null) {
            log.info("批量审核已执行：{}", key);
            result.put("progress", progressValue);
            return ResponseUtil.ok(result);
        }
        // 文件处理
        List<WithdrawOrderAuditDTO> batchList = iWithdrOrderService.file2Data(key);
        // 审核处理
        new Thread(() -> uploadAudit(adminId, key, batchList, orderStatus)).start();
        result.put("progress", "0.01");
        return ResponseUtil.ok(result);
    }

    /**
     * 导出提现订单
     *
     * @param adminId
     * @param withdrDTO
     * @return
     */
    @PostMapping("/export")
    public void export(@LoginAdmin Integer adminId,
                       @RequestBody AdminWithdrDTO withdrDTO,
                       HttpServletResponse response,
                       HttpServletRequest request) throws Exception {
        List<WithdrOrderExportVO> withdrOrderExportVOS = iWithdrOrderService.exportOrder(withdrDTO);
        ReportExcel reportExcel = new ReportExcel();
        reportExcel.excelExport(withdrOrderExportVOS, "WithdrawOrder_" + DateUtil.toyyyy_MM_dd(LocalDateTime.now()), WithdrOrderExportVO.class, 1, response, request);
    }

    /**
     * 提现订单修改为: 提现成功
     *
     * @param adminId
     * @param withdrDTO
     * @return
     */
    @PostMapping("/set/success")
    public Object setSuccess(@LoginAdmin Integer adminId, @RequestBody AdminWithdrDTO withdrDTO){
        log.info("|批量提现置为成功|adminId:{}, 请求报文:{}", adminId, withdrDTO);
        Map<String, Object> retMap = iWithdrOrderService.setSuccess(adminId, withdrDTO);
        return ResponseUtil.ok(retMap);
    }

    /**
     * 提现订单修改为: 提现成功
     *
     * @param adminId
     * @param withdrDTO
     * @return
     */
    @PostMapping("/set/success/batch")
    public Object setSuccessBatch(@LoginAdmin Integer adminId, @RequestBody AdminWithdrDTO withdrDTO){
        log.info("|提现置为成功|adminId:{}, 请求报文:{}", adminId, withdrDTO);
        Map<String, Object> retMap = iWithdrOrderService.setSuccess(adminId, withdrDTO);
        return ResponseUtil.ok(retMap);
    }

    /**
     * 提现订单修改为: 提现成功
     *
     * @param adminId
     * @param fileName
     * @return
     */
    @GetMapping("/set/success/byfile")
    public Object setSuccessByFile(@LoginAdmin Integer adminId,@RequestParam("key") String fileName){
        log.info("|批量提现置为成功|adminId:{}, 请求报文:{}", adminId);
        Map<String, Object> result = new HashMap<>();
        Object progressValue = RedisUtil.get(fileName);
        if (progressValue != null) {
            log.info("批量任务已执行：{}", fileName);
            result.put("progress", progressValue);
            return ResponseUtil.ok(result);
        }
        // 文件数据处理
        List<WithdrawOrderAuditDTO> batchList = iWithdrOrderService.file2Data(fileName);
        // 提现审核处理
        List<String> orderNoList = batchList.stream().map(WithdrawOrderAuditDTO::getOrderNo).collect(toList());
        AdminWithdrDTO withdrOrderDTO = new AdminWithdrDTO();
        withdrOrderDTO.setOrderNos(orderNoList);
        Map<String, Object> retMap = iWithdrOrderService.setSuccess(adminId, withdrOrderDTO);
        return ResponseUtil.ok(retMap);
    }

}
