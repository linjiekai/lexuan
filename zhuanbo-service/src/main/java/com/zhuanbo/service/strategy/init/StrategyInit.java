package com.zhuanbo.service.strategy.init;

import com.zhuanbo.service.strategy.OrderProfitStrategy;
import com.zhuanbo.service.strategy.QuickUpGradeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class StrategyInit {

    /**
     * 普通商品分润
     */
    public static final String NORMAL_ORDER_PROFIT = "normalOrderProfit";
    /**
     * 普通商品运营分润
     */
    public static final String NORMAL_ORDER_OPERATION_PROFIT = "normalOrderOperationProfit";
    /**
     * 礼包分润
     */
    public static final String KESHI_ORDER_PROFIT = "keShiOrderProfit";
    /**
     * 礼包运营分润
     */
    public static final String FOREVER_GIFT_ORDER_PROFIT = "forEverGiftOrderProfit";
    /**
     * 快速充值05类型
     */
    public static final String QUICK_UPGRADE_05 = "quickUpGrade05";
    /**
     * 快速充值06类型
     */
    public static final String QUICK_UPGRADE_06 = "quickUpGrade06";
    /**
     * 快速充值06类型(新)
     */
    public static final String QUICK_UPGRADE_06_N = "quickUpGrade06N";
    /**
     * 新的399分润功能
     */
    public static final String GIFT3_PROFIT_DIVIDE = "gift3ProfitDivide";
    /**
     * 新的600分润功能
     */
    public static final String GIFT6_PROFIT_DIVIDE = "gift6ProfitDivide";

    public static final String NORMAL_PROFIT_DIVIDE = "normalProfitDivide";

    private final Map<String, Object> beansOfType = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        // 多个
        beansOfType.putAll(applicationContext.getBeansOfType(OrderProfitStrategy.class));
        beansOfType.putAll(applicationContext.getBeansOfType(QuickUpGradeStrategy.class));
    }

    public <T> T getByName(String name, Class<T> clazz) {
        return (T) beansOfType.get(name);
    }
}
