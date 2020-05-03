package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.UserQuestion;
import com.zhuanbo.service.mapper.UserQuestionMapper;
import com.zhuanbo.service.service.IUserQuestionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户问题反馈 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@Service
public class UserQuestionServiceImpl extends ServiceImpl<UserQuestionMapper, UserQuestion> implements IUserQuestionService {

}
