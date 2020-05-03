package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Ship;
import com.zhuanbo.service.mapper.ShipMapper;
import com.zhuanbo.service.service.IShipService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 物流公司信息表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
@Service
public class ShipServiceImpl extends ServiceImpl<ShipMapper, Ship> implements IShipService {

}
