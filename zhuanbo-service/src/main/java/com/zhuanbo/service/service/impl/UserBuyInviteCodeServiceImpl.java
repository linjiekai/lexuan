package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zhuanbo.client.server.client.YinLiClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.client.server.dto.common.YLUserDTO;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.PtLevelType;
import com.zhuanbo.core.dto.BuyInviteCodeDTO;
import com.zhuanbo.core.dto.InvestorsPriceDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBuyInviteCode;
import com.zhuanbo.core.enums.DepositOrderTypeEnum;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.MliveClientUtil;
import com.zhuanbo.core.vo.BuyInviteCodeCheckResultVO;
import com.zhuanbo.core.vo.BuyInviteCodeRuleVO;
import com.zhuanbo.core.vo.BuyInviteCodeVO;
import com.zhuanbo.service.mapper.UserBuyInviteCodeMapper;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IUserBuyInviteCodeService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserBuyInviteCodeServiceImpl extends ServiceImpl<UserBuyInviteCodeMapper, UserBuyInviteCode> implements IUserBuyInviteCodeService {

    public static final String ZHUANBO_IN_BU_C_S = "zhuanbo_in_bu_c_s";
    public static final String ZHUANBO_IN_BU_C_S_LIST = "zhuanbo_in_bu_c_s_l";
    public static final String ZHUANBO_IN_BU_C_SC = "zhuanbo_in_bu_c_sc";
    public static final String MSG = "你输入的$1邀请码有效，只需要购买￥$2套餐，即可升级为$3级别。";
    // 分润规则
    public static final List<BuyInviteCodeRuleVO> BUY_INVITE_CODE_RULEVO_LIST = new ArrayList<>();
    // 精油用户等级转面膜等级价格
    public static final Map<Integer, Integer> LevelDepositOrderTypeMap = new HashMap<>();

    static {
        // vip
        BUY_INVITE_CODE_RULEVO_LIST.add(new BuyInviteCodeRuleVO(PtLevelType.VIP.getId(),
                Lists.newArrayList(), DepositOrderTypeEnum.VIP.getId(), DepositOrderTypeEnum.VIP.getId(),
                Lists.newArrayList(DepositOrderTypeEnum.VIP.getId()), false));
        // 店长
        BUY_INVITE_CODE_RULEVO_LIST.add(new BuyInviteCodeRuleVO(PtLevelType.STORE_MANAGER.getId(),
                Lists.newArrayList(PtLevelType.VIP.getId()), DepositOrderTypeEnum.STORE_MANAGER.getId(), DepositOrderTypeEnum.VIP.getId(),
                Lists.newArrayList(DepositOrderTypeEnum.VIP.getId(), DepositOrderTypeEnum.STORE_MANAGER.getId()), false));
        // 总监
        BUY_INVITE_CODE_RULEVO_LIST.add(new BuyInviteCodeRuleVO(PtLevelType.DIRECTOR.getId(),
                Lists.newArrayList(PtLevelType.VIP.getId(), PtLevelType.STORE_MANAGER.getId()), DepositOrderTypeEnum.DIRECTOR.getId(), DepositOrderTypeEnum.VIP.getId(),
                Lists.newArrayList(DepositOrderTypeEnum.VIP.getId(), DepositOrderTypeEnum.DIRECTOR.getId()), false));
        // 合伙人
        BUY_INVITE_CODE_RULEVO_LIST.add(new BuyInviteCodeRuleVO(PtLevelType.PARTNER.getId(),
                Lists.newArrayList(PtLevelType.VIP.getId(), PtLevelType.STORE_MANAGER.getId(), PtLevelType.DIRECTOR.getId()), DepositOrderTypeEnum.PARTNER.getId(), DepositOrderTypeEnum.VIP.getId(),
                Lists.newArrayList(), true));
        // 联创
        BUY_INVITE_CODE_RULEVO_LIST.add(new BuyInviteCodeRuleVO(PtLevelType.BASE.getId(),
                Lists.newArrayList(PtLevelType.VIP.getId(), PtLevelType.STORE_MANAGER.getId(), PtLevelType.DIRECTOR.getId(), PtLevelType.PARTNER.getId()), DepositOrderTypeEnum.BASE.getId(), DepositOrderTypeEnum.VIP.getId(),
                Lists.newArrayList(), true));

        LevelDepositOrderTypeMap.put(PtLevelType.ORDINARY.getId(), DepositOrderTypeEnum.VIP.getId());
        LevelDepositOrderTypeMap.put(PtLevelType.VIP.getId(), DepositOrderTypeEnum.VIP.getId());
        LevelDepositOrderTypeMap.put(PtLevelType.STORE_MANAGER.getId(), DepositOrderTypeEnum.VIP.getId());
        LevelDepositOrderTypeMap.put(PtLevelType.DIRECTOR.getId(), DepositOrderTypeEnum.VIP.getId());
        LevelDepositOrderTypeMap.put(PtLevelType.PARTNER.getId(), DepositOrderTypeEnum.VIP.getId());
        LevelDepositOrderTypeMap.put(PtLevelType.BASE.getId(), DepositOrderTypeEnum.VIP.getId());
        LevelDepositOrderTypeMap.put(PtLevelType.CC.getId(), DepositOrderTypeEnum.VIP.getId());
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private YinLiClient yinLiClient;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IDepositOrderService iDepositOrderService;

    @Override
    public String findCode(Integer ptLevel) {
        Object o = redisTemplate.opsForValue().get(ZHUANBO_IN_BU_C_S + ptLevel);
        if (o != null) {
            return o.toString();
        }
        UserBuyInviteCode userBuyInviteCode = this.getOne(new QueryWrapper<UserBuyInviteCode>().eq("pt_level", ptLevel));
        if (userBuyInviteCode == null) {
            return null;
        }
        redisTemplate.opsForValue().set(ZHUANBO_IN_BU_C_S + ptLevel, userBuyInviteCode.getBuyInviteCodeSuffix(), 1L, TimeUnit.MINUTES);
        return userBuyInviteCode.getBuyInviteCodeSuffix();
    }

    @Override
    public List<UserBuyInviteCode> findAll() {

        Object o = redisTemplate.opsForValue().get(ZHUANBO_IN_BU_C_S_LIST);
        if (o != null) {
            return (List<UserBuyInviteCode>) o;
        }
        List<UserBuyInviteCode> list = list(null);
        if (list == null) {
            return null;
        }
        redisTemplate.opsForValue().set(ZHUANBO_IN_BU_C_S_LIST, list, 1, TimeUnit.MINUTES);
        return list;
    }

    @Override
    public List<BuyInviteCodeVO> makeBuyInviteCodeList(User user) {

        List<UserBuyInviteCode> all = findAll();
        Map<Integer, String> ptLevelCodeMap = all.stream().collect(Collectors.toMap(UserBuyInviteCode::getPtLevel, UserBuyInviteCode::getBuyInviteCodeSuffix));
        ArrayList<BuyInviteCodeVO> buyInviteCodeVOArrayList = new ArrayList<>();

        BuyInviteCodeVO buyInviteCodeVO;
        for (int i = 0; i <= user.getPtLevel(); i++) {
            buyInviteCodeVO = new BuyInviteCodeVO();
            buyInviteCodeVO.setId((long)i);
            buyInviteCodeVO.setPtLevel(i);
            buyInviteCodeVO.setBuyInviteCode(user.getInviteCode() + ptLevelCodeMap.get(i));
            buyInviteCodeVOArrayList.add(buyInviteCodeVO);
        }
        return buyInviteCodeVOArrayList;
    }

    @Override
    public BuyInviteCodeCheckResultVO checkCode(BuyInviteCodeDTO buyInviteCodeDTO) {
        // 普通用户禁止
        User buyer = iUserService.getById(buyInviteCodeDTO.getUserId());
        /*if (PtLevelType.ORDINARY.getId() == buyer.getPtLevel()) {
            throw new ShopException("抱歉，你暂无购买资格，请联系你的上级进行购买");
        }*/
        // 购买码校验
        checkCodeForBuyCode(buyInviteCodeDTO);
        // 规则校验
        return checkCodeForResult(buyInviteCodeDTO, buyer);
    }

    private BuyInviteCodeCheckResultVO checkCodeForResult(BuyInviteCodeDTO buyInviteCodeDTO, User buyer) {

        // 等级不能<=现在的
        String suffixCode = buyInviteCodeDTO.getBuyInviteCode().substring(buyInviteCodeDTO.getBuyInviteCode().length() - 2);
        UserBuyInviteCode userBuyInviteCode = this.findBySuffixCode(suffixCode);
        if (userBuyInviteCode.getPtLevel()  <= buyer.getPtLevel()) {
            throw new ShopException(String.format("你已经是%s等级或以上，请勿重复购买", PtLevelType.toName(userBuyInviteCode.getPtLevel())));
        }
        BuyInviteCodeRuleVO buyInviteCodeRuleVO = null;
        for (BuyInviteCodeRuleVO o : BUY_INVITE_CODE_RULEVO_LIST) {
            if (o.getPtLevel().equals(userBuyInviteCode.getPtLevel())) {
                buyInviteCodeRuleVO = o;
                break;
            }
        }
        if (buyInviteCodeRuleVO == null) {
            log.error("无交易方案");
            throw new ShopException("你输入的购买邀请码有误");
        }
        // 线下
        if (buyInviteCodeRuleVO.getOffLineBuy()) {
            if (buyInviteCodeRuleVO.getEffectiveZhuanboPtLevel().indexOf(buyer.getPtLevel()) != -1) {
                throw new ShopException(String.format("请联系你的上级购买%s套餐", PtLevelType.toName(userBuyInviteCode.getPtLevel())));
            }
            YLUserDTO ylUserDTO = new YLUserDTO();
            ylUserDTO.setMobile(buyer.getMobile());
            ResponseDTO responseDTO = yinLiClient.findByMobile(ylUserDTO);
            if ("10502".equals(responseDTO.getCode())) {
                throw new ShopException("请求失败:引力");
            } else if ("10000".equals(responseDTO.getCode())) {
                return yinliUser2Result(responseDTO);
                /*User yinliUser = JSON.parseObject(JSON.toJSONString(responseDTO.getData()), User.class);
                Integer ptLevel = yinliLevel2ZhuanboLevel(yinliUser.getPtLevel());
                BigDecimal price = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3").getLevelPrice(LevelDepositOrderTypeMap.get(ptLevel));
                String ptLevelName = PtLevelType.toName(ptLevel);
                String msg = MSG.replace("$1", ptLevelName).replace("$3", ptLevelName).replace("$2", String.valueOf(price));
                return new BuyInviteCodeCheckResultVO(LevelDepositOrderTypeMap.get(ptLevel), price, msg, Lists.newArrayList(LevelDepositOrderTypeMap.get(ptLevel)));*/
            }
            throw new ShopException(String.format("请联系你的上级购买%s套餐", PtLevelType.toName(userBuyInviteCode.getPtLevel())));
        }
        // 在可以购买的等级范围内
        int effectiveZhuanboPtLevel = buyInviteCodeRuleVO.getEffectiveZhuanboPtLevel().indexOf(buyer.getPtLevel());
        if (effectiveZhuanboPtLevel != -1) {
            InvestorsPriceDTO investors = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3");
            BigDecimal price = investors.getLevelPrice(buyInviteCodeRuleVO.getEffectiveZhuanboDepositOrderType());
            return makeBuyInviteCodeCheckResultVO(buyInviteCodeRuleVO, userBuyInviteCode.getPtLevel(), price, true);
        }
        // 不在可以购买的等级范围内(普通用户)
        YLUserDTO ylUserDTO = new YLUserDTO();
        ylUserDTO.setMobile(buyer.getMobile());
        ResponseDTO responseDTO = yinLiClient.findByMobile(ylUserDTO);
        // 引力
        if ("10502".equals(responseDTO.getCode())) {
            throw new ShopException("请求失败:引力");
        } else if ("10000".equals(responseDTO.getCode())) {// 精油用户存在
            return yinliUser2Result(responseDTO);
            /*User yinliUser = JSON.parseObject(JSON.toJSONString(responseDTO.getData()), User.class);
            Integer ptLevel = yinliLevel2ZhuanboLevel(yinliUser.getPtLevel());
            BigDecimal price = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3").getLevelPrice(LevelDepositOrderTypeMap.get(ptLevel));
            // return makeBuyInviteCodeCheckResultVO(buyInviteCodeRuleVO, ptLevel, price, true);// 499
            String ptLevelName = PtLevelType.toName(ptLevel);
            String msg = MSG.replace("$1", ptLevelName).replace("$3", ptLevelName).replace("$2", String.valueOf(price));
            return new BuyInviteCodeCheckResultVO(LevelDepositOrderTypeMap.get(ptLevel), price, msg, Lists.newArrayList(LevelDepositOrderTypeMap.get(ptLevel)));*/
        } else {
            // 默认会去注册
            BigDecimal price = BigDecimal.ZERO;
            for (Integer o : buyInviteCodeRuleVO.getNormalZhuanboAndNotExistYinLiDepositOrderType()) {
                BigDecimal levelPrice = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3").getLevelPrice(o);
                price = price.add(levelPrice).setScale(2, RoundingMode.HALF_DOWN);
            }
            return makeBuyInviteCodeCheckResultVO(buyInviteCodeRuleVO, userBuyInviteCode.getPtLevel(), price, false);
        }
    }

    /**
     * @param responseDTO
     * @return
     */
    private BuyInviteCodeCheckResultVO yinliUser2Result(ResponseDTO responseDTO){
        User yinliUser = JSON.parseObject(JSON.toJSONString(responseDTO.getData()), User.class);
        Integer ptLevel = yinliLevel2ZhuanboLevel(yinliUser.getPtLevel());
        BigDecimal price = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3").getLevelPrice(LevelDepositOrderTypeMap.get(ptLevel));
        String ptLevelName = PtLevelType.toName(ptLevel);
        String msg = MSG.replace("$1", ptLevelName).replace("$3", ptLevelName).replace("$2", String.valueOf(price));
        return new BuyInviteCodeCheckResultVO(LevelDepositOrderTypeMap.get(ptLevel), price, msg, Lists.newArrayList(LevelDepositOrderTypeMap.get(ptLevel)));
    }
    /**
     * 引力的等级转赚播等级
     * @param yinliLevel
     * @return
     */
    private Integer yinliLevel2ZhuanboLevel(Integer yinliLevel){
        // yinli - 用户等级0:普通用户;1:VIP;2:县级店;3:品牌店;4:金钻;5:总裁;6:分公司
        // zhuanbo - 用户等级 [0:普通用户, 1:VIP, 2:店长, 3:总监, 4:合伙人, 5:联创]
        switch (yinliLevel) {
            case 0:
            case 1:
            case 2:
                return PtLevelType.VIP.getId();
            case 3:
                return PtLevelType.STORE_MANAGER.getId();
            case 4:
                return PtLevelType.DIRECTOR.getId();
            case 5:
                return PtLevelType.PARTNER.getId();
            case 6:
                return PtLevelType.BASE.getId();
            default:
                return null;
        }
    }

    private BuyInviteCodeCheckResultVO makeBuyInviteCodeCheckResultVO(BuyInviteCodeRuleVO buyInviteCodeRuleVO, Integer userBuyInviteCodePtLevel,
                                                                      BigDecimal price, boolean in) {
        String ptLevelName = PtLevelType.toName(userBuyInviteCodePtLevel);
        String msg = MSG.replace("$1", ptLevelName).replace("$3", ptLevelName).replace("$2", String.valueOf(price));
        List<Integer> ll;
        if (in) {
            ll = Lists.newArrayList(buyInviteCodeRuleVO.getEffectiveZhuanboDepositOrderType());
        } else {
            ll = buyInviteCodeRuleVO.getNormalZhuanboAndNotExistYinLiDepositOrderType();
        }
        return new BuyInviteCodeCheckResultVO(buyInviteCodeRuleVO.getEffectiveZhuanboDepositOrderType(), price, msg, ll);
    }

    /**
     * 购买码校验
     * @param buyInviteCodeDTO
     */
    private void checkCodeForBuyCode(BuyInviteCodeDTO buyInviteCodeDTO){
        if (StringUtils.isBlank(buyInviteCodeDTO.getBuyInviteCode())) {
            throw new ShopException("请输入邀请码");
        }
        // 只能是邀请上级
        String inviteCode = buyInviteCode2InviteCode(buyInviteCodeDTO.getBuyInviteCode());
        User parent = iUserService.findByInviteCode(inviteCode);
        if (parent == null) {
            log.error("{}|购买邀请码|{}|找不到上级", buyInviteCodeDTO.getUserId(), buyInviteCodeDTO.getBuyInviteCode());
            throw new ShopException("你输入的购买邀请码有误");
        }
        // 不能超级
        String suffixCode = buyInviteCodeDTO.getBuyInviteCode().substring(buyInviteCodeDTO.getBuyInviteCode().length() - 2);
        UserBuyInviteCode userBuyInviteCode = this.findBySuffixCode(suffixCode);
        Optional.ofNullable(userBuyInviteCode).orElseThrow(() -> new ShopException("你输入的购买邀请码有误"));
        if (userBuyInviteCode.getPtLevel() > parent.getPtLevel()) {
            log.error("购买码等级一致:{},最高等级：{}，现在等级：{}", buyInviteCodeDTO.getBuyInviteCode(), parent.getPtLevel(), userBuyInviteCode.getPtLevel());
            throw new ShopException("你输入的购买邀请码有误");
        }
        /*UserInvite userInvite = iUserInviteService.getById(buyInviteCodeDTO.getUserId());
        if (!userInvite.getPid().equals(parent.getId())) {
            log.error("{}|购买邀请码|{}|不是他上级", buyInviteCodeDTO.getUserId(), buyInviteCodeDTO.getBuyInviteCode());
            throw new ShopException("请输入邀请上级的购买邀请码");
        }*/
    }

    @Override
    public UserBuyInviteCode findBySuffixCode(String suffixCode) {
        Object o = redisTemplate.opsForValue().get(ZHUANBO_IN_BU_C_SC + suffixCode);
        if (o != null) {
            return (UserBuyInviteCode) o;
        }
        UserBuyInviteCode userBuyInviteCode = getOne(new QueryWrapper<UserBuyInviteCode>().eq("buy_invite_code_suffix", suffixCode));
        if (userBuyInviteCode == null) {
            return null;
        }
        redisTemplate.opsForValue().set(ZHUANBO_IN_BU_C_SC + suffixCode, userBuyInviteCode, 1, TimeUnit.MINUTES);
        return userBuyInviteCode;
    }

    @Override
    public User buyInviteCode2User(String buyInviteCode) {
        return iUserService.findByInviteCode(buyInviteCode2InviteCode(buyInviteCode));
    }

    @Override
    public String buyInviteCode2InviteCode(String buyInviteCode) {
        return buyInviteCode.substring(0, buyInviteCode.length() - 2);
    }

    /**
     * 校验是不是团队的成员
     */
    private void checkPhpTeam() {
        // TODO 完善
        String s;
        try {
            s = null;// = HttpUtil.sendPostJson(authConfig.getPhpTeamInSizeUrl(), null, null);
        } catch (Exception e) {
            log.error("请求异常");
            throw new ShopException("请求异常");
        }
    }
}
