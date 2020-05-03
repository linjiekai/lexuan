package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.AdTemplate;
import com.zhuanbo.core.entity.BlankTemplate;
import com.zhuanbo.service.mapper.AdTemplateMapper;
import com.zhuanbo.service.mapper.BlankTemplateMapper;
import com.zhuanbo.service.service.IAdTemplateService;
import com.zhuanbo.service.service.IBlankTemplateService;
import org.springframework.stereotype.Service;


@Service
public class BlankTemplateServiceImpl extends ServiceImpl<BlankTemplateMapper, BlankTemplate> implements IBlankTemplateService {

}
