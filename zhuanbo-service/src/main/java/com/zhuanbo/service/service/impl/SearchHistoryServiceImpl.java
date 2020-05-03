package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.SearchHistory;
import com.zhuanbo.service.mapper.SearchHistoryMapper;
import com.zhuanbo.service.service.ISearchHistoryService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 搜索历史表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements ISearchHistoryService {

}
