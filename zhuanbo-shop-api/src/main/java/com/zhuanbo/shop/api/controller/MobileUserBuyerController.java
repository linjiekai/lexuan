package com.zhuanbo.shop.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.AESCoder;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBuyer;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IUserBuyerService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.utils.LogOperateUtil;
import com.zhuanbo.shop.api.dto.req.UserBuyerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户订购人信息表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/shop/mobile/user/buyer")
@Slf4j
public class MobileUserBuyerController {

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IUserBuyerService iUserBuyerService;

    @Autowired
    private ICashService iCashService;

    @Autowired
    private IDictionaryService dictionaryService;

    /**
     * 添加订购人信息
     *
     * @param userId
     * @return
     */
    @PostMapping("/add")
    public Object add(@LoginUser Long userId, @RequestBody UserBuyerDTO userBuyerDTO) {
        LogOperateUtil.log("订购人信息", "添加订购人", String.valueOf(userId), userId, 0);
        User user = iUserService.getById(userId);
        if (user == null) {
            return ResponseUtil.badResult();
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_USER_REAL_NAME.String());
        params.put(ReqResEnum.REQUEST_ID.String(), System.currentTimeMillis());
        params.put("userId", userId);
        params.put("name", userBuyerDTO.getName());
        params.put("cardNo", userBuyerDTO.getCardNo());
        params.put("cardType", userBuyerDTO.getCardType());
        params.put("imgFront", userBuyerDTO.getImgFront());
        params.put("imgBack", userBuyerDTO.getImgBack());
        params.put("realSource", userBuyerDTO.getRealSource());
        params.put("realType", 1);
        params.put("sysCnl", userBuyerDTO.getSysCnl());
        params.put("clientIp", userBuyerDTO.getClientIp());
        params.put("timestamp", DateUtil.getSecondTimestamp(System.currentTimeMillis()));
        params.put(ReqResEnum.MERC_ID.String(), authConfig.getMercId());
        params.put(ReqResEnum.PLATFORM.String(), userBuyerDTO.getPlatform());

        log.info("|订购人信息|添加订购人|订购人实名认证|");
        JSONObject retJson = iCashService.send(params);
        if (retJson == null) {
            log.error("调用实名认证失败");
            throw new ShopException(40011);
        }
        String retCode = (String) retJson.get("code");
        String retMsg = (String) retJson.get("msg");
        if (!Constants.SUCCESS_CODE.equals(retCode)) {
            log.error("调用实名认证失败, 失败原因:{}", retMsg);
            throw new ShopException(40011);
        }

        String cardNoAbbr;
        String aesKey = dictionaryService.findForString("SecretKey", "AES");
        String aesIv = dictionaryService.findForString("SecretKey", "IV");
        try {
            cardNoAbbr = AESCoder.decrypt(userBuyerDTO.getCardNo(), aesKey, aesIv);
        } catch (Exception e) {
            throw new ShopException(40011);
        }
        cardNoAbbr = cardNoAbbr.substring(0, 3) + "******"
                + cardNoAbbr.substring(cardNoAbbr.length() - 4, cardNoAbbr.length());

        UserBuyer userBuyer = new UserBuyer();
        userBuyer.setName(userBuyerDTO.getName());
        userBuyer.setCardNo(userBuyerDTO.getCardNo());
        userBuyer.setCardNoAbbr(cardNoAbbr);
        userBuyer.setCardType(userBuyerDTO.getCardType());
        userBuyer.setImgBack(userBuyerDTO.getImgBack());
        userBuyer.setImgFront(userBuyerDTO.getImgFront());
        userBuyer.setUserId(userId);

        log.info("|订购人信息|添加订购人|保存:{}|", userBuyer);
        iUserBuyerService.save(userBuyer);

        return ResponseUtil.ok(userBuyer);
    }

    /**
     * 订购人列表信息
     *
     * @param userId       用户id
     * @param userBuyerDTO name    订购人姓名
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginUser Long userId, @RequestBody UserBuyerDTO userBuyerDTO) {
        LogOperateUtil.log("订购人信息", "查询订购人列表", String.valueOf(userId), userId, 0);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("name", userBuyerDTO.getName());
        Page<UserBuyer> page = new Page<>(userBuyerDTO.getPage(), userBuyerDTO.getLimit());
        IPage<UserBuyer> userBuyerIPage = iUserBuyerService.pageCustom(page, params);

        Map<String, Object> result = new HashMap<>();
        result.put("total", userBuyerIPage.getTotal());
        result.put("items", userBuyerIPage.getRecords());
        return ResponseUtil.ok(result);
    }

}
