package com.zhuanbo.service.strategy.ext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.service.*;
import com.zhuanbo.service.strategy.OrderProfitStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 普通订单的运营商分润策略
 */
@Component
public class NormalOrderOperationProfit extends OrderProfitStrategy {

    // 分润运营关系
    private final String[] OPE_LEVEL = {"low", "middle", "high"};
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);// 忽略不存在的字段
    private final Long ZERO = 0L;

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IUserPartnerProfitRuleService iUserPartnerProfitRuleService;

    // 停用
    @Override
    public Map<String, Object> orderProfit(Order order, Long cid, List<OrderGoods> orderGoodsList, Map<String, Object> params) throws Exception{
        return null;
        /*LogUtil.SHARE_PROFIT.info("No:{},普通商品运营分润啦", order.getOrderNo());
        // 普通用户运营分润(销售费)
        UserOperLevel userOperLevel = iUserOperLevelService.selectProfitOperParent(order.getUserId());
        if (userOperLevel == null) {
            LogUtil.SHARE_PROFIT.info("No:{},普通商品运营分润啦, 分润关系找不到", order.getOrderNo());
            return null;
        }
        Map<String, Object> map = objectMapper.convertValue(userOperLevel, Map.class);
        LogUtil.SHARE_PROFIT.info("No:{},普通商品运营分润啦, 分润关系：{}", order.getOrderNo(), map);
        Goods goods = iGoodsService.getById(orderGoodsList.get(0).getGoodsId());

        int times = 1;
        for (String s : OPE_LEVEL) {
            if (times > 2) {
                break;
            }
            Object o = map.get(s);
            if (o == null) {
                continue;
            }
            Long uid = (Long) o;
            if (ZERO.equals(uid) || cid.equals(uid)) {
                continue;
            }
            User u = iUserService.getById(uid);
            BigDecimal price = makePrice(u, goods);
            iUserIncomeDetailsService.saveXiaoShouIncomeDetails(order, u, price, IncomeDetailEnum.MODE_TYPE_1.Integer());

            UserIncome userIncome = iUserIncomeService.getUserIncome(uid);
            if (iUserIncomeService.updateShareIncome(userIncome.getId(), price) == 0) {
                throw new ShopException("销售分润更新失败");
            }
            times++;
        }
        // 通知
        return null;*/
    }

    /**
     * 返回指定用户的分润规则对应的钱
     * @param user 用户
     * @param goods 商品
     * @return
     */
    private BigDecimal makePrice(User user, Goods goods) {
        return null;
        /*BigDecimal b100 = new BigDecimal("100");
        if (UserEnum.PT_OPER_LEVEL_1.Integer().equals(user.getPtOperLevel())) {
            return goods.getPrice().multiply(goods.getLow().divide(b100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else if (UserEnum.PT_OPER_LEVEL_2.Integer().equals(user.getPtOperLevel())) {
            return goods.getPrice().multiply(goods.getMiddle().divide(b100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else if (UserEnum.PT_OPER_LEVEL_3.Integer().equals(user.getPtOperLevel())) {
            return goods.getPrice().multiply(goods.getHigh().divide(b100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            return new BigDecimal("0.00");
        }*/
    }
}