package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.LiveChannel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 直播频道创建表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
public interface LiveChannelMapper extends BaseMapper<LiveChannel> {

    /**
     * 增加在线人数
     * @param cId
     * @return
     */
    @Update("update shop_live_channel set on_line_user_number = on_line_user_number + 1 where c_id = #{cId} ")
    int updateOnLineUserNumberUp(@Param("cId") String cId);

    /**
     * 减少在线人数
     * @param cId
     * @return
     */
    @Update("update shop_live_channel set on_line_user_number = on_line_user_number - 1 where c_id = #{cId} and on_line_user_number > 0 ")
    int updateOnLineUserNumberDown(@Param("cId") String cId);
}
