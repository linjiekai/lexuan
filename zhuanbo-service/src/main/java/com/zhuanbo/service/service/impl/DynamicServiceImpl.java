package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Dynamic;
import com.zhuanbo.service.mapper.DynamicMapper;
import com.zhuanbo.service.service.IDynamicService;
import com.zhuanbo.service.vo.DynamicVO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-04-04
 */
@Service
public class DynamicServiceImpl extends ServiceImpl<DynamicMapper, Dynamic> implements IDynamicService {


    @Override
    public Page<DynamicVO> list(Page<DynamicVO> page, Integer userId) {
        page.setRecords(baseMapper.list(page, userId));
        return page;
    }

    @Override
    public int updateLikeNumber(Long id, int number) {
        return baseMapper.updateLikeNumber(id, number);
    }

    @Override
    public String toHowLongTime(LocalDateTime time) {
        if(time != null){
            Duration duration = Duration.between(time,LocalDateTime.now());
            if(duration.toHours() == 0 && duration.toMinutes()>0){
                return duration.toMinutes()+"分钟前";
            }else if(duration.toHours() > 0 && duration.toHours()< 24 && duration.toMinutes()>0){
                return duration.toHours()+"小时"+ (duration.toMinutes() - duration.toHours() * 60)+"分钟前";
            }else if(duration.toHours() > 24 && duration.toHours() < 24*30){
                return (int)duration.toHours()/24+"天前";
            }else if(duration.toHours() > 24*30 && duration.toHours() < 24*30*12){
                return(int)duration.toHours()/ (24*30) +"月前";
            }else if(duration.toHours() > 24*30*12){
                return(int)duration.toHours()/ (24*30*12)+"年前";
            }else{
                return "刚刚";
            }
        }
        return "未知时间";
    }
}
