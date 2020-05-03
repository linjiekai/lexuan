package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.PushReceiver;
import com.zhuanbo.service.mapper.PushReceiverMapper;
import com.zhuanbo.service.service.IPushReceiverService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 推送接收者表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-04-11
 */
@Service
public class PushReceiverServiceImpl extends ServiceImpl<PushReceiverMapper, PushReceiver> implements IPushReceiverService {

}
