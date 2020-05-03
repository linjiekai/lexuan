package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.UserVirtual;
import com.zhuanbo.service.mapper.UserVirtualMapper;
import com.zhuanbo.service.service.IUserVirtualService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 虚拟用户基础信息表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-14
 */
@Service
public class UserVirtualServiceImpl extends ServiceImpl<UserVirtualMapper, UserVirtual> implements IUserVirtualService {

}
