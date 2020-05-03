package com.zhuanbo.service.vo;

import com.zhuanbo.core.vo.BuyInviteCodeVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录返回数据DTO
 */
@Data
public class UserLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String accid;
    private String userName;
    private String headImgUrl;
    private String nickname;
    private String name;
    private Integer gender;
    private String mobile;
    private String telPhone;
    private String email;
    private String birthday;
    private String userToken;
    private String token;
    private String openId;
    private Integer hasPwd = 0;// 是否设置过密码。0：未设置，1：已设置
    private String areaCode;
    private String wxName;// 微信名称
    private String wxOpenId;
    private Integer ptLevel;
    private String ptNo;
    private String inviteCode;
    private Integer cardType;
    private String cardNoAbbr;
    private String authNo;
    private String authDate;
    private Integer realed;
    private String teamName;
    private Integer ptFormal;
    /**
     * 是否进行签名 [0:否, 1:是]
     */
    private Integer signed;
    /**
     * 签名图片url地址
     */
    private String signImgUrl;
    private LocalDateTime signTime;
    private Long liveUserId;
    // 购买邀请码
    private List<BuyInviteCodeVO> buyInviteCodeList;

    private Long inviteUpUserId;
}
