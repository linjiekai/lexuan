package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.WxToken;
import com.zhuanbo.service.mapper.WxTokenMapper;
import com.zhuanbo.service.service.IWxTokenService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
public class WxTokenServiceImpl extends ServiceImpl<WxTokenMapper, WxToken> implements IWxTokenService {

}
