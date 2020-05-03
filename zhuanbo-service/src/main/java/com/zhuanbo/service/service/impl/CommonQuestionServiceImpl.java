package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.CommonQuestion;
import com.zhuanbo.service.mapper.CommonQuestionMapper;
import com.zhuanbo.service.service.ICommonQuestionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 常见问题表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-14
 */
@Service
public class CommonQuestionServiceImpl extends ServiceImpl<CommonQuestionMapper, CommonQuestion> implements ICommonQuestionService {

}
