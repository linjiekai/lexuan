package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.BuyInviteCodeDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserBuyInviteCode;
import com.zhuanbo.core.vo.BuyInviteCodeCheckResultVO;
import com.zhuanbo.core.vo.BuyInviteCodeVO;

import java.util.List;

public interface IUserBuyInviteCodeService extends IService<UserBuyInviteCode> {
    /**
     * 获取尾缀码
     * @param ptLevel
     * @return
     */
    String findCode(Integer ptLevel);

    /**
     * 获取所有
     * @return
     */
    List<UserBuyInviteCode> findAll();

    /**
     * 根据用户等级生成购买邀请码
     * @param user
     * @return
     */
    List<BuyInviteCodeVO> makeBuyInviteCodeList(User user);

    /**
     * 校验购买邀请码
     * @param buyInviteCodeDTO
     * @return
     */
    BuyInviteCodeCheckResultVO checkCode(BuyInviteCodeDTO buyInviteCodeDTO);

    /**
     * 根据suffixCode获取数据
     * @param suffixCode
     * @return
     */
    UserBuyInviteCode findBySuffixCode(String suffixCode);

    /**
     * 购买邀请码转用户
     * @param buyInviteCode
     * @return
     */
    User buyInviteCode2User(String buyInviteCode);

    /**
     * 购买邀请码转邀请码
     * @param buyInviteCode
     * @return
     */
    String buyInviteCode2InviteCode(String buyInviteCode);
}
