package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Token;
import com.zhuanbo.service.mapper.TokenMapper;
import com.zhuanbo.service.service.ITokenService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * token令牌 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@Service
public class TokenServiceImpl extends ServiceImpl<TokenMapper, Token> implements ITokenService {

}
