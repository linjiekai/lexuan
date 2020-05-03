package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.SeqIncr;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 序列号表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface SeqIncrMapper extends BaseMapper<SeqIncr> {

    /**
     * 下一条序列
     * @param seqName
     * @return
     */
    @Select("select nextval(#{seqName})")
    long nextVal(@Param("seqName") String seqName);

    /**
     * 当前最大的序列
     * @param seqName
     * @return
     */
    @Select("select currval(#{seqName})")
    long currVal(@Param("seqName") String seqName);

}
