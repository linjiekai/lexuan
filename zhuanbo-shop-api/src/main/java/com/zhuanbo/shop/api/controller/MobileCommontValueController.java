package com.zhuanbo.shop.api.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.dto.PayDictionaryDTO;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.entity.UserProfitRule;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.MliveClientUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IPayDictionaryService;
import com.zhuanbo.service.service.IUserProfitRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/shop/mobile/")
@RefreshScope
@Slf4j
public class MobileCommontValueController {

    @Value("${serverPhone}")
    private String serverPhone;

    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    private IUserProfitRuleService iUserProfitRuleService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IPayDictionaryService iPayDictionaryService;
    
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    
    @PostMapping("/commonValue")
    public Object commonValue() {
    	Map<String, Object> map = (Map<String, Object>) RedisUtil.get(Constants.COMMON_VALUE);
    	if (null == map) {
    		try {
    			boolean lockFlag = false;
            	
            	lockFlag = redissonLocker.tryLock(Constants.COMMON_VALUE_LOCK, TimeUnit.SECONDS, Constants.LOCK_WAIT_TIME, Constants.LOCK_LEASE_TIME);
            	
            	if (!lockFlag) {
            		log.error("获取分布式失败lockFlag=" + lockFlag + ",seqName=" + Constants.COMMON_VALUE);
    				throw new ShopException("11201");
    			}
            	
            	map = (Map<String, Object>) RedisUtil.get(Constants.COMMON_VALUE);
            	
            	if (null == map) {
            		map = getCommonValue();
            		RedisUtil.set(Constants.COMMON_VALUE, map, 60 * 5);
            	}
    		} finally {
    			redissonLocker.unlock(Constants.COMMON_VALUE_LOCK);
    		}
    	}
        // 返回信息
        return ResponseUtil.ok(map);
    }
    
    private Map<String, Object> getCommonValue() {
    	// 创建返回数据map
        Map<String, Object> map = new HashMap<>();

        // 电话号码
        map.put("serverPhone",StringUtils.stripToEmpty(serverPhone));

        // 是否开放升级活动
        map.put("activityUpgradeCheck", iDictionaryService.findForLong(ConstantsEnum.ACTIVITY_UPGRADE.stringValue(), ConstantsEnum.ACTIVITY_UPGRADE_CHECK.stringValue()));
        map.put("withdrBankStar2Exper", new BigDecimal(iDictionaryService.findForString(ConstantsEnum.ACTIVITY_UPGRADE.stringValue(), ConstantsEnum.STAR2EXPER.stringValue())));
        map.put("withdrBankReach2Exper", new BigDecimal(iDictionaryService.findForString(ConstantsEnum.ACTIVITY_UPGRADE.stringValue(), ConstantsEnum.REACH2EXPER.stringValue())));
        map.put("depositPrice", MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3").getLevelAndPrice());

        // 立赚
        UserProfitRule profitType = iUserProfitRuleService.getOne(new QueryWrapper<UserProfitRule>().eq("profit_type", ConstantsEnum.PROFIT_TYPE_1.integerValue()));
        map.put("quickPirce", JSON.parseObject(profitType.getContent()).getBigDecimal("plus"));

        // 是否开放银行卡快捷支付
        Dictionary quickPayFlag = iDictionaryService.getOne(new QueryWrapper<Dictionary>()
                .eq("category",ConstantsEnum.QUICK_PAY.stringValue())
                .eq("name", "open"));
        Long quickPayCheck = quickPayFlag == null ? 0 : quickPayFlag.getLongVal();
        map.put("quickPayCheck", quickPayCheck);

        // 小程序二维码
        map.put("mpQrCode", iDictionaryService.findForString(ConstantsEnum.MP.stringValue(), ConstantsEnum.QRCODE.stringValue()));

        // 提现数据处理
        List<PayDictionaryDTO> withdrDicList = iPayDictionaryService.list(new PayDictionaryDTO("withdr"));
        if (withdrDicList != null && withdrDicList.size() > 0) {
            // 提现数据: 提现手续费费率
            withdrDicList.stream().filter(dic -> "commision_ratio".equals(dic.getName())).forEach(dic -> map.put("withdrCommission", dic.getLongVal()));
            // 提现数据: 可提现银行是否开放
            withdrDicList.stream().filter(dic -> "bank_open".equals(dic.getName())).forEach(dic -> map.put("withdrBankCheck", dic.getLongVal()));
            // 提现数据: 是否开放[提现到银行卡]
            withdrDicList.stream().filter(dic -> "to_bank_open".equals(dic.getName())).forEach(dic -> map.put("withdrToBankCheck", dic.getLongVal()));
            // 提现数据: 提现最大金额
            withdrDicList.stream().filter(dic -> "price_max".equals(dic.getName())).forEach(dic -> map.put("withdrMaxPrice", dic.getLongVal()));
            // 提现数据: 提现最低金额
            withdrDicList.stream().filter(dic -> "price_min".equals(dic.getName())).forEach(dic -> {
                BigDecimal priceMin = dic.getStrVal() == null ? BigDecimal.ZERO : new BigDecimal(dic.getStrVal());
                priceMin.setScale(2, BigDecimal.ROUND_HALF_UP);
                map.put("withdrMinPrice", priceMin);
            });
            // ip白名单
            withdrDicList.stream().filter(dic -> "ip_whitelist".equals(dic.getName())).forEach(dic -> map.put("withdrIpWhitelist", dic.getStrVal()));
        }

        // 随机立减数据处理
        List<PayDictionaryDTO> reduceDicList = iPayDictionaryService.list(new PayDictionaryDTO("reduce"));
        if (reduceDicList != null && reduceDicList.size() > 0) {
            // 随机立减数据: 微信
            reduceDicList.stream().filter(dic -> "weixin".equals(dic.getName())).forEach(dic -> map.put("reduceWeixin", dic.getStrVal()));
            // 随机立减数据: 支付宝
            reduceDicList.stream().filter(dic -> "alipay".equals(dic.getName())).forEach(dic -> map.put("reduceAlipay", dic.getStrVal()));
            // 随机立减数据: 银行卡
            reduceDicList.stream().filter(dic -> "card".equals(dic.getName())).forEach(dic -> map.put("reduceCard", dic.getStrVal()));
        }

        // 是否开放面膜业务入口
        Long maskBusOpen = iDictionaryService.findForLong(ConstantsEnum.MASK_BUSINESS.stringValue(), ConstantsEnum.ENTRANCE_OPEN.stringValue());
        map.put("maskBusinessEntranceOpen", maskBusOpen);

        // 开放面膜: ip白名单
        if (Long.valueOf(0).equals(maskBusOpen)) {
            String clientIp = (String) MDC.get("CLIENT_IP");
            log.info("|面膜ip白名单校验|接收到用户ip:{}", clientIp);
            String withdrIpWhitelist = (String) map.get("withdrIpWhitelist");
            String[] ipWhitelist = withdrIpWhitelist.split(",");
            List<String> ipWhitelists = Arrays.asList(ipWhitelist);
            if (ipWhitelists.contains(clientIp)) {
                map.put("maskBusinessEntranceOpen", 1);
            }
        }
        
        return map;
    }
}
