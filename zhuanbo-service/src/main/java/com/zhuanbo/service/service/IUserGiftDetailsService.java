package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.GiftAdjustDTO;
import com.zhuanbo.core.dto.GiftDTO;
import com.zhuanbo.core.dto.GiftDetailDTO;
import com.zhuanbo.core.entity.UserGiftDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * <p>
 * 用户礼包明细表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-11-22
 */
public interface IUserGiftDetailsService extends IService<UserGiftDetails> {

    /**
     * 列表
     * @param giftDTO
     * @return
     */
    Object list(GiftDTO giftDTO);

    /**
     * 指标变更列表
     * @param page
     * @param ew
     * @return
     */
    IPage<GiftDetailDTO> changeList(Page<GiftDetailDTO> page, Map<String, Object> ew);

    /**
     * 新增指标
     * @param dto
     * @return
     */
    @Transactional
    boolean addChangeList(GiftDetailDTO dto);

    /**
     * 调整礼包
     * @param dto
     * @return
     */
    @Transactional
    boolean giftAdjust(GiftAdjustDTO dto);
}
