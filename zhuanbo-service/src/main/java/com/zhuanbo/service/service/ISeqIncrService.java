package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.entity.SeqIncr;

/**
 * <p>
 * 序列号表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface ISeqIncrService extends IService<SeqIncr> {

    /**
     * 下一条序列
     * @param seqName
     * @return
     */
    String nextVal(String seqName, int length, Align align);

    /**
     * 当前最大的序列
     * @param seqName
     * @return
     */
    String currVal(String seqName, int length, Align align);
    
    
    String paddingVal(String value, int length, Align align);
}
