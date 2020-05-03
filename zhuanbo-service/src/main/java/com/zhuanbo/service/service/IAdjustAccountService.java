package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.AdjustAccount;
import com.zhuanbo.service.vo.AdjustAccountVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 调怅记录表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-09-24
 */
public interface IAdjustAccountService extends IService<AdjustAccount> {

    Page<AdjustAccountVO> list(Page<AdjustAccountVO> page, Map<String, Object> params);

    /**
     * 添加
     * @param adjustAccount
     */
    void addOne(Integer adminId, AdjustAccount adjustAccount) throws Exception;

    /**
     * 手工调账
     */
    Map<String, List<AdjustAccount>> manualAdd();
}
