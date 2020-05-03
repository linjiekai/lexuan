package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Component;
import com.zhuanbo.service.mapper.ComponentMapper;
import com.zhuanbo.service.service.IComponentService;
import org.springframework.stereotype.Service;


@Service
public class ComponentServiceImpl extends ServiceImpl<ComponentMapper, Component> implements IComponentService {

}
