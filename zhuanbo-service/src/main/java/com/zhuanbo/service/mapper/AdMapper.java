package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.Ad;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * <p>
 * 广告表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface AdMapper extends BaseMapper<Ad> {

    @Select("select * from shop_ad where #{now} > start_time and #{now}< end_time and position = 1 ORDER BY add_time ASC LIMIT 1")
    Ad getStartupPageAd(@Param("now") LocalDateTime now);

    @Select("select * from shop_ad where #{now} > start_time and #{now}< end_time and position = 3 ORDER BY add_time ASC LIMIT 1")
    Ad getMyAd(@Param("now") LocalDateTime now);


    @Select("update shop_ad set status = 2 where #{now} > start_time and #{now}< end_time and position = #{position} and (status = 1 or status = 2) ORDER BY add_time ASC LIMIT 1")
    void updateAdToEffect(@Param("now") LocalDateTime now, Integer position);



}
