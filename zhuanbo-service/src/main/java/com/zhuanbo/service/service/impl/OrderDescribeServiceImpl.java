package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.OrderDescribe;
import com.zhuanbo.service.mapper.OrderDescribeMapper;
import com.zhuanbo.service.service.IOrderDescribeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单副表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@Service
public class OrderDescribeServiceImpl extends ServiceImpl<OrderDescribeMapper, OrderDescribe> implements IOrderDescribeService {

}
