package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserPartner;
import com.zhuanbo.service.mapper.UserPartnerMapper;
import com.zhuanbo.service.service.IUserPartnerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 合伙人基础信息表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-08-19
 */
@Service
public class UserPartnerServiceImpl extends ServiceImpl<UserPartnerMapper, UserPartner> implements IUserPartnerService {

    private final DateTimeFormatter YYYYMMDD =  DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter YYYYMMDDCN =  DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Override
    public int updatePtEffNum(Long id, int number) {
        return baseMapper.updatePtEffNum(id, number);
    }

    @Override
    public Long simpleGenerate(User user) {

        UserPartner userPartnerTmp = getById(user.getId());
        if(null != userPartnerTmp){
            BeanUtils.copyProperties(user, userPartnerTmp);
            updateById(userPartnerTmp);
            return userPartnerTmp.getId();
        }

        UserPartner userPartner = new UserPartner();
        BeanUtils.copyProperties(user, userPartner);
        userPartner.setPtEffNum(0);
        userPartner.setAddTime(LocalDateTime.now());
        save(userPartner);

        return userPartner.getId();
    }

    @Override
    public int updatePtEffNumMax20(Long id, int number, long limit) {
        return baseMapper.updatePtEffNumMax20(id, number, limit);
    }

    @Override
    public int haveTypes(Long id, Integer... types) {
        return haveTypes(getById(id), types);
    }

    @Override
    public int haveTypes(UserPartner userPartner, Integer... types) {
        int sum = 0;
        for (Integer t : types) {
            sum += userPartner.getPurchasedType() & t;
        }
        return sum;
    }

    @Override
    public int updatePurchasedType(Long uid, Integer purchasedType) {
        return baseMapper.updatePurchasedType(uid, purchasedType);
    }

    @Override
    public int updateAuthDateAntType(UserPartner userPartner, Integer purchasedType) {
        return baseMapper.updateAuthDateAntType(userPartner, purchasedType);
    }

    @Override
    public void generateAutoNo(User user) {
        // 授权编号
        LocalDateTime now = LocalDateTime.now();
        UserPartner userPartner =getById(user.getId());
        userPartner.setAuthNo(now.format(YYYYMMDD) + user.getInviteCode());
        userPartner.setAuthDate(now.format(YYYYMMDDCN));
        updateAuthDateAntType(userPartner, 0);
    }
}
