package com.zhuanbo.shop.api.controller;


import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.dto.BuyInviteCodeDTO;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.vo.BuyInviteCodeCheckResultVO;
import com.zhuanbo.service.service.IUserBuyInviteCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/shop/mobile/buyinvitecode")
@Slf4j
@RefreshScope
public class MobileUserBuyInviteCodeController {

    @Autowired
    private IUserBuyInviteCodeService iUserBuyInviteCodeService;

    /**
     * 购买邀请码确认
     * @param userId
     * @param buyInviteCodeDTO
     * @return
     */
    @PostMapping("/check")
    public Object check(@LoginUser Long userId, @RequestBody BuyInviteCodeDTO buyInviteCodeDTO) {

        buyInviteCodeDTO.setUserId(userId);
        BuyInviteCodeCheckResultVO buyInviteCodeCheckResultVO = iUserBuyInviteCodeService.checkCode(buyInviteCodeDTO);
        return ResponseUtil.ok(buyInviteCodeCheckResultVO);
    }
}
