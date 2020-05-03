package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.MqMessage;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * MQ处理异常消息 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-07-10
 */
public interface MqMessageMapper extends BaseMapper<MqMessage> {
    /**
     * 更新状态
     * @param status
     * @param uuid
     * @return
     */
    @Update("update shop_mq_message set status = #{status} where uuid = #{uuid}")
    int updateStatusByUUID(Integer status, String uuid);
}
