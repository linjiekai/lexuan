package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;import com.zhuanbo.core.entity.OrderRefund;
import com.zhuanbo.service.mapper.OrderRefundMapper;
import com.zhuanbo.service.service.IOrderRefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单与条关联表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-10-29
 */
@Service
@Slf4j
public class OrderRefundServiceImpl extends ServiceImpl<OrderRefundMapper, OrderRefund> implements IOrderRefundService {

}
