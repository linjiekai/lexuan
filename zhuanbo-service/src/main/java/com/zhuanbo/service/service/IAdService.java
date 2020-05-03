package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminAdDTO;
import com.zhuanbo.core.entity.Ad;
import com.zhuanbo.service.vo.AdVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 广告表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IAdService extends IService<Ad> {

    Ad getStartupPageAd(LocalDateTime now);

    Ad getMyAd(LocalDateTime now);

    void updateAdToEffect(LocalDateTime now, Integer position);

    List<AdVO> getAdList(Integer position, String platform);


    /**
     * 定时修改状态(前一天的)
     */
    void modifyStatus();

    void saveAds2Redis();
   //创建
    Object saveAds(AdminAdDTO dto)throws Exception;
    //更新
    Object updateAds(AdminAdDTO dto)throws Exception;
    //更新
    Object queryAdsList(AdminAdDTO dto)throws Exception;
}
