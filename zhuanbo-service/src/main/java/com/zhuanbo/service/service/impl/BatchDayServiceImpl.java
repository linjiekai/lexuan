package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.BatchDay;
import com.zhuanbo.service.mapper.BatchDayMapper;
import com.zhuanbo.service.service.IBatchDayService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 日切表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-27
 */
@Service
public class BatchDayServiceImpl extends ServiceImpl<BatchDayMapper, BatchDay> implements IBatchDayService {

}
