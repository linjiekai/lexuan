package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.UserBindThird;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.service.mapper.UserBindThirdMapper;
import com.zhuanbo.service.service.IUserBindThirdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 用户绑定第三方账号表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@Service
@Slf4j
public class UserBindThirdServiceImpl extends ServiceImpl<UserBindThirdMapper, UserBindThird> implements IUserBindThirdService {

    @Override
    public UserBindThird findOne(Long uid, String bindType) {
        List<UserBindThird> list = list(new QueryWrapper<UserBindThird>().eq("user_id", uid).eq("bind_type", bindType));
        if (list.size() > 1){
            log.error("绑定数据重复|{}|{}", uid, bindType);
            throw new ShopException("绑定数据重复");
        }
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public UserBindThird findOneByUnionidOrBindType(String unionid, String bindType) {
        QueryWrapper<UserBindThird> userBindThirdQueryWrapper = new QueryWrapper<>();
        Optional.ofNullable(StringUtils.trimToNull(unionid)).ifPresent(x -> userBindThirdQueryWrapper.eq("bind_id", x));
        Optional.ofNullable(StringUtils.trimToNull(bindType)).ifPresent(x -> userBindThirdQueryWrapper.eq("bind_type", x));
        return getOne(userBindThirdQueryWrapper);
    }

    @Override
    public void copyOne(UserBindThird userBindThird, String bindType, String openid) {
        UserBindThird newUserBindThird = new UserBindThird();
        BeanUtils.copyProperties(userBindThird, newUserBindThird);
        newUserBindThird.setBindType(bindType);
        newUserBindThird.setAddTime(LocalDateTime.now());
        newUserBindThird.setUpdateTime(newUserBindThird.getAddTime());
        Optional.ofNullable(StringUtils.stripToNull(openid)).ifPresent(x -> newUserBindThird.setOpenId(x));
        save(newUserBindThird);
    }
}
