package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.UserEnum;
import com.zhuanbo.core.entity.*;
import com.zhuanbo.service.mapper.UserProfitRuleMapper;
import com.zhuanbo.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 用户利润分配规则表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
public class UserProfitRuleServiceImpl extends ServiceImpl<UserProfitRuleMapper, UserProfitRule> implements IUserProfitRuleService {

    final String PLUS_TRAIN = "plus-train"; // 达 - 体
    final String PLUS_SERV = "plus-serv";// 达 - 司
    final String TRAIN_TRAINEQ = "train-trainEq";// 体 - 平体
    final String TRAIN_SERV = "train-serv";// 体 - 司
    final String TRAINEQ_SERV = "trainEq-serv";// 平体 - 司
    final String SERV_SERVEQ = "serv-servEq";// 司 - 平司
    final String PARTNER = "partner";// M司令(合伙人)
    final String HIGH_PARTNER = "highPartner";// M司令(高级合伙人)
    final String DIRECTOR = "director";// M司令(总监)
    final String HIGH_DIRECTOR = "highDirector";// M司令(高级总监)
    final String SPOKESMAN = "spokesman";// M司令(代言人)
    final String HIGH_SPOKESMAN = "highSpokesman";// M司令(高级代言人)
    // 新的等级
    final List<String> NEW_LEVEL = Arrays.asList("partner", "highPartner", "director", "highDirector", "spokesman", "highSpokesman");

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IOrderGoodsService iOrderGoodsService;
    @Autowired
    private IAdminService iAdminService;


    @Override
    public void create(Integer adminId, String string, Integer type) {

        LocalDateTime now = LocalDateTime.now();
        UserProfitRule userProfitRule = this.getOne(new QueryWrapper<UserProfitRule>().eq("mode_type", type));
        BigDecimal plus = null;
        if (ConstantsEnum.PROFIT_TYPE_1.integerValue().equals(type)) {
            JSONObject jsonObject = JSON.parseObject(string);
            plus = jsonObject.getBigDecimal("plus");
        }

        if (userProfitRule == null) {
            userProfitRule = new UserProfitRule();
            userProfitRule.setModeType(type);
            userProfitRule.setContent(string);
            userProfitRule.setAddTime(now);
            userProfitRule.setUpdateTime(now);
            userProfitRule.setOperator(iAdminService.getAdminName(adminId));
            userProfitRule.setPlus(plus);
            this.save(userProfitRule);
        } else {
            userProfitRule.setContent(string);
            userProfitRule.setUpdateTime(now);
            userProfitRule.setOperator(iAdminService.getAdminName(adminId));
            userProfitRule.setUpdateTime(LocalDateTime.now());
            if (plus != null) {
                userProfitRule.setPlus(plus);
            }
            this.updateById(userProfitRule);
        }
    }

    /**
     *  300礼包分润关系
     * @param user 开始用户
     * @param userLevel 开始用户的user_level
     * @param userProfitRule 收益分配规则
     * @param cid 公司账号
     * @return
     */
    private List<Map<String, Object>> generateIdPrice2(User user, UserLevel userLevel, UserProfitRule userProfitRule, Long cid, String orderNo) {

        LogUtil.SHARE_PROFIT.info("No:{}，正常分润，generateIdPrice：userLevel：{}", orderNo, userLevel);

        List<String> level = Arrays.asList("plus", "train", "trainEq", "serv", "servEq");
        Map<String, Object> beanMap = JSON.parseObject(JSON.toJSONString(userLevel), Map.class);

        Map<String, Long> levelIdMap = new HashMap<>();// {level:id}
        String zero = "0";
        for (String s : level) {
            if (beanMap.get(s) == null || zero.equals(beanMap.get(s).toString())) {
                continue;
            }
            levelIdMap.put(s, Long.valueOf(beanMap.get(s).toString()));
        }

        String userCurrentLevel;// 当前用户对应的层级角色
        Integer userCurrentLevelIndex = 0;// 当前用户对应的层级角色的位置订单完成MQ处理开始，M星人自己购买的商品, 分享赚钱 OK

        Iterator<Map.Entry<String, Long>> iterator = levelIdMap.entrySet().iterator();

        LogUtil.SHARE_PROFIT.info("No:{}，正常分润，levelIdMap：{}", orderNo, levelIdMap);
        // 找出当前用户所在的级别，如plus等
        while (iterator.hasNext()) {
            Map.Entry<String, Long> next = iterator.next();
            if (next.getValue() != null && user.getId().equals(next.getValue())) {
                userCurrentLevel = next.getKey();
                userCurrentLevelIndex = level.indexOf(userCurrentLevel);
                break;
            }
        }
        LogUtil.SHARE_PROFIT.info("No:{}，userCurrentLevelIndex:{}", orderNo, userCurrentLevelIndex);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> m;
        String firstLevel = level.get(userCurrentLevelIndex);
        LogUtil.SHARE_PROFIT.info("No:{}，firstLevel:{}", orderNo, firstLevel);
        Long uid;
        String eq = "Eq";

        for(int i = userCurrentLevelIndex; i < level.size(); i++) {
            LogUtil.SHARE_PROFIT.info("level.get(i):{}, levelIdMap.get(level.get(i)):{}", level.get(i), levelIdMap.get(level.get(i)));
            uid = levelIdMap.get(level.get(i));
            if (uid == null) {
                continue;
            }
            if (uid.equals(cid)) {// 公司账号
                break;
            }
            if (String.valueOf(level.get(i)).endsWith(eq)) {// // 平级的要判断是否是有效的平级
                // 当前平级的前一个ID与当前ID的关系
                int nowLevelIndex = level.indexOf(String.valueOf(level.get(i)));// 当前层级在level中的位置
                if (nowLevelIndex > 0) {
                    Long sameLevelFrontUId = Long.valueOf(levelIdMap.get(level.get(nowLevelIndex - 1)).toString());
                    User currentUser = iUserService.getById(uid);
                    User sameLevelUser = iUserService.getById(sameLevelFrontUId);
                    if (!currentUser.getPtLevel().equals(sameLevelUser.getPtLevel())) {
                        LogUtil.SHARE_PROFIT.info("订单完成MQ处理开始currentUser:{}和sameLevelUser:{}不是平级，级别分别是{}, {}", currentUser.getId(), sameLevelUser.getId(), currentUser.getPtLevel(), sameLevelUser.getPtLevel());
                        continue;
                    } else {
                        if (iUserInviteService.directLevelUserNumber(currentUser.getId(), currentUser.getPtLevel()) < 2) {
                            LogUtil.SHARE_PROFIT.info("订单完成MQ处理开始currentUser:{}不是>=2", currentUser.getId());
                            continue;
                        }
                    }
                }
            }
            LogUtil.SHARE_PROFIT.info("开始循环, 正常走");
            m = new HashMap<>();
            m.put("id", uid);
            m.put("price", getPriceByLevel(firstLevel+"-"+level.get(i), userProfitRule));
            list.add(m);
            LogUtil.SHARE_PROFIT.info("开始循环, 正常走，hashMap:{}", m);
            firstLevel = String.valueOf(level.get(i));
        }
        return list;
    }

    /**
     * 获取普通商品订单的下的分润信息
     * @param user
     * @param userLevel
     * @param cid
     * @param orderGoodsList
     * @return
     */
    private List<Map<String, Object>> generateIdPrice3(User user, UserLevel userLevel, Long cid, List<OrderGoods> orderGoodsList) {

        LogUtil.SHARE_PROFIT.info("订单完成MQ处理开始，正常分润，generateIdPrice：userLevel：{}, generateIdPrice3", userLevel);

        List<String> level = Arrays.asList("plus", "train", "trainEq", "serv", "servEq", "partner", "highPartner", "director", "highDirector", "spokesman", "highSpokesman");
        Map<String, Object> beanMap = JSON.parseObject(JSON.toJSONString(userLevel), Map.class);
        Map<String, Long> levelIdMap = new HashMap<>();// {level:id}
        LogUtil.SHARE_PROFIT.info("UserLevel:Map:{}", levelIdMap);
        String zero = "0";
        for (String s : level) {// 根据user_level找出每个等级对应的用户id
            if (beanMap.get(s) == null || zero.equals(beanMap.get(s).toString())) {
                continue;
            }
            levelIdMap.put(s, Long.valueOf(beanMap.get(s).toString()));
        }
        LogUtil.SHARE_PROFIT.info("levelIdMap:{}", levelIdMap);

        Integer userCurrentLevelIndex;// 当前用户的等级在level数据中的位置
        // 找出当前用户所在的级别，如plus等，然后从后面开始寻找用户的分润关系
        switch (UserEnum.getOne(user.getPtLevel(), UserEnum.PT_LEVEL.Type())) {
            case PT_LEVEL_1:
                userCurrentLevelIndex = 0;
                break;
            case PT_LEVEL_2:
                userCurrentLevelIndex = 1;
                break;
            case PT_LEVEL_3:
                userCurrentLevelIndex = 2;
                break;
            case PT_LEVEL_4:
                userCurrentLevelIndex = 3;
                break;
            case PT_LEVEL_5:
                userCurrentLevelIndex = 4;
                break;
            case PT_LEVEL_6:
                userCurrentLevelIndex = 5;
                break;
            case PT_LEVEL_7:
                userCurrentLevelIndex = 6;
                break;
            case PT_LEVEL_8:
                userCurrentLevelIndex = 7;
                break;
            case PT_LEVEL_9:
                userCurrentLevelIndex = 8;
                break;
            default:
                throw new RuntimeException("获取用户层级异常");
        }

        LogUtil.SHARE_PROFIT.info("userCurrentLevelIndex:{}", userCurrentLevelIndex);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> m;
        String firstLevel = level.get(userCurrentLevelIndex);
        LogUtil.SHARE_PROFIT.info("firstLevel:{}", firstLevel);
        Long uid;
        String eq = "Eq";
        LogUtil.SHARE_PROFIT.info("开始循环:orderGoodsList:{}", orderGoodsList);

        Map<String, BigDecimal> priceMap = new HashMap<>();
        String h0 = "0.00";
        BigDecimal number;
        BigDecimal plusTrain = new BigDecimal(h0);// M达人
        BigDecimal plusServ = new BigDecimal(h0);//
        BigDecimal trainTrainEq = new BigDecimal(h0);
        BigDecimal trainServ = new BigDecimal(h0);
        BigDecimal trainEqServ = new BigDecimal(h0);
        BigDecimal servServEq = new BigDecimal(h0);
        BigDecimal partner = new BigDecimal(h0);// M司令(合伙人)
        BigDecimal highPartner = new BigDecimal(h0);// M司令(高级合伙人)
        BigDecimal director = new BigDecimal(h0);// M司令(总监)
        BigDecimal highDirector = new BigDecimal(h0);// M司令(高级总监)
        BigDecimal spokesman = new BigDecimal(h0);// M司令(代言人)
        BigDecimal highSpokesman = new BigDecimal(h0);// M司令(高级代言人)
        for (OrderGoods orderGoods : orderGoodsList) {// 每个分润关系分多少钱
            if (ConstantsEnum.GOODS_TYPE_1.integerValue().equals(orderGoods.getGoodsType())) {
                continue;
            }
            number = new BigDecimal(orderGoods.getNumber());
            plusTrain = plusTrain.add(orderGoods.getTrain().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            plusServ = plusServ.add(orderGoods.getServIndt().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            trainTrainEq = trainTrainEq.add(orderGoods.getTrainEq().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            trainServ = trainServ.add(orderGoods.getServLower().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            trainEqServ = trainEqServ.add(orderGoods.getServLower().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            servServEq = servServEq.add(orderGoods.getServEq().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            partner = partner.add(orderGoods.getPartner().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            highPartner = highPartner.add(orderGoods.getHighPartner().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            director = director.add(orderGoods.getDirector().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            highDirector = highDirector.add(orderGoods.getHighPartner().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            spokesman = spokesman.add(orderGoods.getSpokesman().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
            highSpokesman = highSpokesman.add(orderGoods.getHighSpokesman().multiply(number).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        priceMap.put(PLUS_TRAIN, plusTrain);
        priceMap.put(PLUS_SERV, plusServ);
        priceMap.put(TRAIN_TRAINEQ, trainTrainEq);
        priceMap.put(TRAIN_SERV, trainServ);
        priceMap.put(TRAINEQ_SERV, trainEqServ);
        priceMap.put(SERV_SERVEQ, servServEq);
        priceMap.put(PARTNER, partner);
        priceMap.put(HIGH_PARTNER, highPartner);
        priceMap.put(DIRECTOR, director);
        priceMap.put(HIGH_DIRECTOR, highDirector);
        priceMap.put(SPOKESMAN, spokesman);
        priceMap.put(HIGH_SPOKESMAN, highSpokesman);
        LogUtil.SHARE_PROFIT.info("等级与金钱：priceMap:{}", priceMap);

        for(int i = userCurrentLevelIndex; i < level.size(); i++) {
            LogUtil.SHARE_PROFIT.info("level.i:{},get(i):{}, levelIdMap.get(level.get(i)):{}", i, level.get(i), levelIdMap.get(level.get(i)));
            uid = levelIdMap.get(level.get(i));
            if (uid == null) {
                continue;
            }
            if (uid.equals(cid)) {// 公司账号
                break;
            }
            LogUtil.SHARE_PROFIT.info("开始循环,id:{}", uid );
            if (String.valueOf(level.get(i)).endsWith(eq)) {// // 平级的要判断是否是有效的平级
                // 当前平级的前一个ID与当前ID的关系
                int nowLevelIndex = level.indexOf(String.valueOf(level.get(i)));// 当前层级在level中的位置
                if (nowLevelIndex > 0) {
                    Long sameLevelFrontUId = Long.valueOf(levelIdMap.get(level.get(nowLevelIndex - 1)).toString());
                    User currentUser = iUserService.getById(uid);
                    User sameLevelUser = iUserService.getById(sameLevelFrontUId);
                    if (!currentUser.getPtLevel().equals(sameLevelUser.getPtLevel()) || currentUser.getId().equals(sameLevelUser.getId())) {
                        LogUtil.SHARE_PROFIT.info("订单完成MQ处理开始currentUser:{}和sameLevelUser:{}不是平级，级别分别是{}, {}", currentUser.getId(), sameLevelUser.getId(), currentUser.getPtLevel(), sameLevelUser.getPtLevel());
                        continue;
                    } else {
                        if (iUserInviteService.directLevelUserNumber(currentUser.getId(), currentUser.getPtLevel()) < 2) {
                            LogUtil.SHARE_PROFIT.info("订单完成MQ处理开始currentUser:{}不是>=2", currentUser.getId());
                            continue;
                        }
                    }
                }
            }
            m = new HashMap<>();
            m.put("id", uid);
            m.put("price", getPriceByLevel2(firstLevel+"-"+level.get(i), priceMap));
            list.add(m);
            LogUtil.SHARE_PROFIT.info("等级与金钱：level:{},hashMap:{}", firstLevel+"-"+level.get(i), m);
            firstLevel = String.valueOf(level.get(i));
        }
        return list;
    }

    /**
     * 根据层级关系获取对应的分润价格（只有效于，达人，平级直属达人，司令，平级直属司令）
     * @param level
     * @param userProfitRule
     * @return
     */
    private BigDecimal getPriceByLevel(String level, UserProfitRule userProfitRule) {
        LogUtil.SHARE_PROFIT.info("getPriceByLevel,level:{}, userProfitRule:{}", level, userProfitRule);
        if (userProfitRule.getProfitType().equals(1)) {
            switch (level) {// 邀请
                case "plus-plus": // M体验官推荐
                    return userProfitRule.getPlus();
                case "train-train":// M达人直接推荐
                    return userProfitRule.getTrain();
                case "serv-serv":// M司令直接推荐
                    return userProfitRule.getServ();
                case "plus-train":// M达人间接推荐
                    return userProfitRule.getTrainIndt();
                case "plus-serv":// M司令间接推荐
                    return userProfitRule.getServIndt();
                case "train-trainEq":// M达人平级直属推荐
                    return userProfitRule.getTrainEq();
                case "train-serv":// M司令下级M达人团队推荐
                    return userProfitRule.getServLower();
                case "trainEq-serv":// M司令下级M达人团队推荐
                    return userProfitRule.getServLower();
                case "serv-servEq":// M司令平级直属推荐
                    return userProfitRule.getServEq();
                default:
                    return new BigDecimal("0.00");
            }
        } else {
            switch (level) {// 销售
                case "plus-train": // 直属M达人团队销售业绩奖励
                    return userProfitRule.getTrain().divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                case "plus-serv": // 直属M达人团队销售业绩奖励
                    return userProfitRule.getServ().divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                case "train-trainEq": // 平级直属M体验官团队销售业绩奖励
                    return userProfitRule.getTrainEq().divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                case "train-serv": // 直属M体验官团队销售业绩奖励
                    return userProfitRule.getServLower().divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                case "trainEq-serv": // 直属M体验官团队销售业绩奖励
                    return userProfitRule.getServLower().divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                case "serv-servEq": // 直属M体验官团队销售业绩奖励
                    return userProfitRule.getServEq().divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                default:
                    return new BigDecimal("0.00");
            }
        }
    }

    private BigDecimal getPriceByLevel2(String level, Map<String, BigDecimal> priceMap) {
        String last = level.split("-")[1];
        if (NEW_LEVEL.contains(last)) {
            level = last;
        }
        if (priceMap.containsKey(level)) {
            return priceMap.get(level);
        } else {
            return new BigDecimal(0);
        }
    }
}
