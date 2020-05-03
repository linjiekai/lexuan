package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.PayInterfaceEnum;
import com.zhuanbo.core.constants.PlatformIncomeTypeEnum;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.constants.WithdrOrderStatusEnum;
import com.zhuanbo.core.constants.WithdrToBankEnum;
import com.zhuanbo.core.constants.WithdrToWeixinOpenEnum;
import com.zhuanbo.core.dto.AdminWithdrDTO;
import com.zhuanbo.core.dto.CardBindLastBindDTO;
import com.zhuanbo.core.dto.WithdrDicDTO;
import com.zhuanbo.core.dto.WithdrawOrderAuditDTO;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.storage.Storage;
import com.zhuanbo.core.util.AESCoder;
import com.zhuanbo.core.util.ApplicationYmlUtil;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.service.mapper.WithdrOrderMapper;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IPayDictionaryService;
import com.zhuanbo.service.service.IPlatformIncomeDetailsService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service.IWithdrOrderService;
import com.zhuanbo.service.vo.WithdrOrderExportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 * 提现订单表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
@Slf4j
public class WithdrOrderServiceImpl extends ServiceImpl<WithdrOrderMapper, WithdrOrder> implements IWithdrOrderService {

    /**
     * 文件名后缀: .xls
     */
    private static final String FILE_NAME_SUFFIX = ".xls";
    /**
     * 银行编号
     */
    private static final String BANK_CODE_WEIXIN = "WEIXIN";
    private static final String BANK_CODE_ALIPAY = "ALIPAY";

    @Autowired
    private ICashService iCashService;
    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    @Qualifier("aliyunStorage")
    private Storage aliyunStorage;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IPayDictionaryService iPayDictionaryService;
    @Autowired
    private IPlatformIncomeDetailsService iPlatformIncomeDetailsService;

    @Override
    public List<WithdrawOrderAuditDTO> file2Data(String fileName) {
        List<WithdrawOrderAuditDTO> resultList = new ArrayList<>();
        InputStream fileStream = null;
        try{
            // 获取文件数据
            Resource resource = aliyunStorage.loadAsResource(fileName);
            fileStream = resource.getInputStream();

            // 文件数据处理
            DataFormatter dataFormatter = new DataFormatter();
            Workbook workbook;
            if (fileName.endsWith(FILE_NAME_SUFFIX)) {
                workbook = new HSSFWorkbook(fileStream);
            } else {
                workbook = new XSSFWorkbook(fileStream);
            }
            // 页
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                Sheet sheetNext = sheetIterator.next();
                // 行
                Iterator<Row> rowIterator = sheetNext.rowIterator();
                while (rowIterator.hasNext()) {
                    Row rowNext = rowIterator.next();
                    // 第一列单号
                    Cell cell = rowNext.getCell(0);
                    String value = dataFormatter.formatCellValue(cell).trim();
                    if (StringUtils.isNotBlank(value)) {
                        resultList.add(new WithdrawOrderAuditDTO(value));
                    }
                }
            }
        }catch (Exception e) {
            log.error("OSS资源获取/解析失败，keyName：{}", fileName);
            throw new ShopException("OSS资源获取/解析失败");
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultList;
    }

    /**
     * 导出订单
     *
     * @param withdrDTO
     * @return
     */
    @Override
    public List<WithdrOrderExportVO> exportOrder(AdminWithdrDTO withdrDTO) throws Exception {
        log.info("|提现订单导出|接收到请求报文:{}", withdrDTO);
        // 查询提现信息
        List<WithdrOrderExportVO> withdrMapList = baseMapper.exportOrder(withdrDTO);
        // 查询账户信息
        List<Long> userIds = withdrMapList.stream().map(WithdrOrderExportVO::getUserId).distinct().collect(toList());
        JSONArray balanceArr = iCashService.balanceBatch(userIds);
        Map<Long, List<Map>> balanceMapByUserId = new HashMap<>();
        if (balanceArr != null) {
            List<Map> balanceMapList = balanceArr.toJavaList(Map.class);
            balanceMapByUserId = balanceMapList.stream().collect(groupingBy(m -> Long.valueOf((Integer) m.get("userId"))));
        }
        // 获取加解密信息
        String aesKey = iDictionaryService.findForString("SecretKey", "AES");
        String aesIv = iDictionaryService.findForString("SecretKey", "IV");

        // 查询绑卡信息
        List<Long> userids = withdrMapList.stream().map(WithdrOrderExportVO::getUserId).collect(toList());
        Map<String, Object> map = new HashMap<>();
        map.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        map.put("userIds", userids);
        String payIp = authConfig.getPayUrlIp();
        String url = payIp + PayInterfaceEnum.LAST_BIND_BY_USERIDS.getId();
        log.info("|提现订单导出|调用MPPAY接口,查询绑卡信息|请求 url:{}, header: {},参数:{}|", url, JacksonUtil.objTojson(map));
        String respMsg = HttpUtil.sendPostJson(url, map, new HashMap<>());
        log.info("|提现订单导出|调用MPPAY接口,查询绑卡信息|结果：{}|", respMsg);
        JSONObject respObject = JSONObject.parseObject(respMsg);
        List<CardBindLastBindDTO> lastBindDTOList;
        Map<Long, List<CardBindLastBindDTO>> lastBindMapByUserId = new HashMap<>();
        if (Constants.SUCCESS_CODE.equals(respObject.getString("code"))) {
            JSONArray respArray = respObject.getJSONArray("data");
            lastBindDTOList = respArray.toJavaList(CardBindLastBindDTO.class);
            lastBindMapByUserId = lastBindDTOList.stream().collect(groupingBy(CardBindLastBindDTO::getUserId));
        }

        // 数据整合
        String bankCardNo;
        String cardNo;
        String mobile;
        List<Map> balanceMapListTemp;
        for (WithdrOrderExportVO withdr : withdrMapList) {
            if (balanceMapByUserId != null && balanceMapByUserId.size() > 0) {
                balanceMapListTemp = balanceMapByUserId.get(withdr.getUserId());
                if (balanceMapListTemp != null && balanceMapListTemp.size() > 0) {
                    Map<String, Object> balanceMapTemp = (Map<String, Object>) balanceMapListTemp.get(0);
                    if (balanceMapTemp != null) {
                        withdr.setAcBal((BigDecimal) balanceMapTemp.get("acBal"));
                        withdr.setWithdrBal((BigDecimal) balanceMapTemp.get("withdrBal"));
                    }
                }
            }
            if (lastBindMapByUserId != null && lastBindMapByUserId.size() > 0) {
                List<CardBindLastBindDTO> lastBindByUserIdList = lastBindMapByUserId.get(withdr.getUserId());
                if (lastBindByUserIdList != null && lastBindByUserIdList.size() > 0) {
                    CardBindLastBindDTO cardBindLastBindDTO = lastBindByUserIdList.get(0);
                    if (cardBindLastBindDTO != null) {
                        String bankCardName = cardBindLastBindDTO.getBankCardName();
                        withdr.setBankCardName(bankCardName);
                        withdr.setBankCodeName(cardBindLastBindDTO.getBankAbbr());
                        withdr.setBankCode(cardBindLastBindDTO.getBankCode());
                        withdr.setCardTypeName(cardBindLastBindDTO.getCardTypeName());
                        bankCardNo = cardBindLastBindDTO.getBankCardNo();
                        cardNo = cardBindLastBindDTO.getCardNo();
                        mobile = cardBindLastBindDTO.getMobile();
                        // 银行卡,身份证解密
                        try{
                            if (bankCardNo != null) {
                                withdr.setBankCardNo(bankCardNo);
                                withdr.setBankCardNoText(AESCoder.decrypt(bankCardNo, aesKey, aesIv));
                            }
                            if (cardNo != null) {
                                withdr.setCardNo(cardNo);
                                withdr.setCardNoText(AESCoder.decrypt(cardNo, aesKey, aesIv));
                            }
                            if (mobile != null) {
                                withdr.setMobile(AESCoder.decrypt(mobile, aesKey, aesIv));
                            }
                        } catch (Exception e) {
                            log.info("银行卡/身份证解密失败，银行卡:{}, 身份证:{}", bankCardNo, cardNo, mobile);
                        }
                    }
                }

            }
            // 订单状态转换
            withdr.setOrderStatusName(WithdrOrderStatusEnum.getNameById(withdr.getOrderStatus()));
            // 提现时间处理
            LocalDateTime addTime = withdr.getAddTime();
            withdr.setWithdrDate(addTime.toLocalDate().toString());
            withdr.setWithdrTime(addTime.toLocalTime().toString());
        }
        return withdrMapList;
    }

    /**
     * 订单状态置为
     *
     * @param withdrDTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> setSuccess(Integer adminId, AdminWithdrDTO withdrDTO) {
        log.info("|提现订单置为成功|adminId:{}, 请求报文:{}", adminId, withdrDTO);
        Map<String, Object> result = new HashMap<>();
        List<WithdrawOrderAuditDTO> retErrorList = new ArrayList<>();
        QueryWrapper<WithdrOrder> queryWrapper = new QueryWrapper<>();
        Long id = withdrDTO.getId();
        String startTime = withdrDTO.getStartTime();
        String endTime = withdrDTO.getEndTime();
        Integer userId = withdrDTO.getUserId();
        String nickname = withdrDTO.getNickname();
        List<String> orderNos = withdrDTO.getOrderNos();
        List<Long> ids = withdrDTO.getIds();

        Optional.ofNullable(id).ifPresent(s -> queryWrapper.eq("id", s));
        Optional.ofNullable(StringUtils.stripToNull(startTime)).ifPresent(s -> queryWrapper.ge("add_time", s));
        Optional.ofNullable(StringUtils.stripToNull(endTime)).ifPresent(s -> queryWrapper.le("add_time", s));
        Optional.ofNullable(userId).ifPresent(uid -> queryWrapper.eq("user_id",uid));
        Optional.ofNullable(StringUtils.stripToNull(nickname)).ifPresent(s -> {
            List<User> userList = iUserService.list(new QueryWrapper<User>().like("nickname", s).eq("deleted", 0));
            if (CollectionUtils.isNotEmpty(userList)) {
                List<Long> userIds = userList.stream().map(User::getId).collect(toList());
                queryWrapper.in("user_id", userIds);
            }
        });
        if (orderNos != null && orderNos.size() > 0) {
            queryWrapper.in("order_no", orderNos);
        }
        if (ids != null && ids.size() > 0) {
            queryWrapper.in("id", ids);
        }
        List<WithdrOrder> withdrOrderList = this.list(queryWrapper);

        // 不存在订单号
        if (orderNos != null && orderNos.size() > 0) {
            List<String> realOrderNoList = withdrOrderList.stream().map(WithdrOrder::getOrderNo).collect(toList());
            List<String> nonexistentOrderNos = orderNos.stream().filter(orderNo -> !realOrderNoList.contains(orderNo)).collect(toList());
            if (nonexistentOrderNos != null && nonexistentOrderNos.size() > 0) {
                nonexistentOrderNos.forEach(withdrOrder -> {
                    WithdrawOrderAuditDTO withdrDto = new WithdrawOrderAuditDTO();
                    withdrDto.setOrderNo(withdrOrder);
                    withdrDto.setAuditMsg("订单号不存在");
                    retErrorList.add(withdrDto);
                });
            }
        }

        // 订单状态不正确
        List<WithdrOrder> errorWithdrOrders = withdrOrderList.stream().filter(withdrOrder -> !WithdrOrderStatusEnum.WAIT.getId().equals(withdrOrder.getOrderStatus())).collect(toList());
        if (errorWithdrOrders != null && errorWithdrOrders.size() > 0) {
            errorWithdrOrders.forEach(withdrOrder -> {
                WithdrawOrderAuditDTO withdrDto = new WithdrawOrderAuditDTO();
                withdrDto.setOrderNo(withdrOrder.getOrderNo());
                withdrDto.setOrderStatus(withdrOrder.getOrderStatus());
                withdrDto.setAuditMsg("订单状态不正确,订单状态:" + WithdrOrderStatusEnum.getNameById(withdrOrder.getOrderStatus()));
                retErrorList.add(withdrDto);
            });
        }

        // 处理有效订单号
        List<WithdrOrder> handleWithdrOrders = withdrOrderList.stream().filter(withdrOrder -> WithdrOrderStatusEnum.WAIT.getId().equals(withdrOrder.getOrderStatus())).collect(toList());
        Admin admin = iAdminService.getById(adminId);
        handleWithdrOrders.forEach(order -> {
            order.setOrderStatus(WithdrOrderStatusEnum.THIRD_SUCCESS.getId());
            order.setAuditTime(LocalDateTime.now());
            order.setAuditor(admin.getUsername());
            order.setAuditorId(adminId);
        });
        if (handleWithdrOrders != null && handleWithdrOrders.size() > 0) {
            boolean updFlag = this.updateBatchById(handleWithdrOrders);
            if (!updFlag) {
                throw new ShopException("[提现置为成功]执行失败");
            }
        }

        if (retErrorList.size() > 0) {
            result.put("extraCode", "-1");
            Map<String, Object> dmap = new HashMap<>();
            dmap.put("cnheader", Lists.newArrayList("订单号", "原因"));
            dmap.put("enheader", Lists.newArrayList("orderNo", "auditMsg"));
            dmap.put("list", retErrorList);
            result.put("extraData", dmap);
        }
        result.put("progress", "1.00");
        return result;
    }

    /**
     * 单人单日提现总额
     *
     * @param userId
     * @param addTime
     * @return
     */
    @Override
    public BigDecimal sumPriceByUserIdAndAddTime(Long userId, LocalDate addTime) {
        QueryWrapper<WithdrOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(price) AS price")
                .notIn("order_status", WithdrOrderStatusEnum.FAIL.getId(), WithdrOrderStatusEnum.ERROR.getId())
                .eq("user_id", userId)
                .eq("DATE(add_time)", addTime);
        WithdrOrder withdrOrder = this.getOne(queryWrapper);
        if (withdrOrder == null || withdrOrder.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return withdrOrder.getPrice();
    }

    /**
     * @param userId
     */
    @Override
    public void withdrApplyCheck(Long userId, BigDecimal price, WithdrDicDTO withdrDic, String bankCode) {
        if (withdrDic == null) {
            log.error("|提现申请|提现配置字典获取失败|用户id:{}", userId);
            throw new ShopException(51017);
        }

        if (StringUtils.isBlank(bankCode)) {
            log.error("|提现申请|协议号或银行编号无效|用户id:{},bankCode:{}", userId, bankCode);
            throw new ShopException(51018);
        }

        // 提现开放
        Long bankOpen = withdrDic.getBankOpen();
        if (bankOpen == null || 0 == bankOpen) {
            log.error("|提现申请|可提现银行卡未开放(该功能即将上线，敬请期待)|用户id:{},bankCode:{}", userId, bankCode);
            throw new ShopException(51019);
        }

        // 提现开关校验
        switch(bankCode){
            case BANK_CODE_WEIXIN:
                // 微信提现开关 [0:关闭, 1:开放]
                Long toWeixinOpen = withdrDic.getToWeixinOpen();
                Optional.ofNullable(toWeixinOpen).orElseThrow(() -> {
                    log.error("|提现申请|微信提现开关未配置|");
                    return new ShopException(51013);
                });
                if (toWeixinOpen == WithdrToWeixinOpenEnum.OFF.getId()) {
                    throw new ShopException(51012);
                }
                break;
            case BANK_CODE_ALIPAY:
                break;
            default:
                // 银行卡提现开关 [0:关闭, 1:开放]
                Long toBankOpen = withdrDic.getToBankOpen();
                Optional.ofNullable(toBankOpen).orElseThrow(() -> {
                    log.error("|提现申请|提现到银行卡未配置开关信息|");
                    return new ShopException(51011);
                });
                if (toBankOpen == (WithdrToBankEnum.OFF.getId())) {
                    throw new ShopException(51010);
                }
                break;
        }

        // 单日提现次数限制
        LocalDateTime now = LocalDateTime.now();
        int count = this.count(new QueryWrapper<WithdrOrder>().eq("user_id", userId).eq("order_date", DateUtil.toyyyy_MM_dd(now)));
        Long limitCount = withdrDic.getTimesMax() - 1;
        if (count > limitCount) {
            throw new ShopException(51003, "1天只能提现" + (limitCount + 1) + "笔");
        }

        BigDecimal maxWithdrPrice = withdrDic.getPriceMax();
        if (maxWithdrPrice.compareTo(price) == -1) {
            log.error("|提现申请|提现金额不能大于限额:{}|", maxWithdrPrice);
            throw new ShopException(51002, ApplicationYmlUtil.get(51002) + "￥" + maxWithdrPrice);
        }
        BigDecimal minWithdrPrice = withdrDic.getPriceMin();
        if (minWithdrPrice.compareTo(price) == 1) {
            log.error("|提现申请|提现金额不能小于限额:{}|", minWithdrPrice);
            throw new ShopException(51009, "每次提现额度必须大于" + minWithdrPrice + "元!");
        }

        // 余额限制
        JSONObject bal = iCashService.balance(userId);
        BigDecimal acBal = bal.getBigDecimal("acBal");
        if (acBal == null || acBal.compareTo(price) == -1) {
            log.error("用户{}余额{}", userId, acBal);
            throw new ShopException(51004);
        }

        if (1==1){
            // TODO ==============================================================================================================
            // TODO 以下规则暂不生效 日期:(2020-04-18 15:17:00)  (微信提现开关 [0:关闭, 1:开放], 平台每日提现限额, 单人每日提现限额, 单人单次提现限额)
            // TODO ==============================================================================================================
            return;
        }

        // 平台每日提现限额
        BigDecimal platformDayLimit = withdrDic.getPlatformDayLimit();
        BigDecimal allWithdrPrice = iPlatformIncomeDetailsService.sumPriceByIncomeTypeAndAddTime(PlatformIncomeTypeEnum.WITHDR.getId(), LocalDate.now());
        allWithdrPrice = allWithdrPrice.add(price);
        if (allWithdrPrice.compareTo(platformDayLimit) > -1) {
            log.error("|提现申请|平台每日提现额度已达上限");
            throw new ShopException(51014);
        }

        // 单人每日提现限额
        BigDecimal personDayLimit = withdrDic.getPersonDayLimit();
        BigDecimal allWithdrPriceDay = this.sumPriceByUserIdAndAddTime(userId, LocalDate.now());
        allWithdrPriceDay = allWithdrPriceDay.add(price);
        if (allWithdrPriceDay.compareTo(personDayLimit) > -1) {
            log.error("|提现申请|单人每日提现额度已达上限,提现金额:{}, 限额:{}", allWithdrPriceDay, personDayLimit);
            throw new ShopException(51016);
        }

        // 单人单次提现限额
        BigDecimal personSingleLimit = withdrDic.getPersonSingleLimit();
        if (price.compareTo(personSingleLimit) == 1) {
            log.error("|提现申请|单人单笔提现:{},超过限额:{}", price, personSingleLimit);
            throw new ShopException(51015, "单笔提现额度不得超过" + personSingleLimit + "元");
        }
    }
}
