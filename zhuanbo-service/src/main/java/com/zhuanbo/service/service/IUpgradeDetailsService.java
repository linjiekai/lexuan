package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.UpgradeDetails;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
import com.zhuanbo.service.vo.UpgradeDetailsVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 升级费明细表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-07-31
 */
public interface IUpgradeDetailsService extends IService<UpgradeDetails> {

    /**
     * 根据支付回调返回的信息生成记录
     * @param payNotifyParamsVO
     */
    Map<String, Object> generateDetail(PayNotifyParamsVO payNotifyParamsVO);
    /**
     * 管理后台获取升级费明细列表
     * @param page
     * @param limit
     * @param userId
     * @param mobile
     * @return
     */
    List<UpgradeDetailsVo> findUpgradeDetails(Integer page, Integer limit, Long userId, String mobile);

    /**
     *  统计直属达人的数量
     * @param userId
     * @return
     */
    Integer countDarenNum(Long userId);

    /**
     * 统计后台充值总记录数
     * @param userId
     * @param mobile
     * @return
     */
    Integer countUpgradeTotalRecords(Long userId, String mobile);

    /**
     *根据手机号码获取用户信息
     * @param mobile
     * @return
     */
    List<UpgradeDetailsVo> findUserByMobile(String mobile);

}
