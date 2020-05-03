package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminWithdrDTO;
import com.zhuanbo.core.dto.WithdrDicDTO;
import com.zhuanbo.core.dto.WithdrawOrderAuditDTO;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.service.vo.WithdrOrderExportVO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 提现订单表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IWithdrOrderService extends IService<WithdrOrder> {

    /**
     * 文件数据处理
     * @param fileName
     * @return
     * @throws IOException
     */
    List<WithdrawOrderAuditDTO> file2Data(String fileName);

    /**
     * 导出订单
     *
     * @param withdrDTO
     * @return
     */
    List<WithdrOrderExportVO> exportOrder(AdminWithdrDTO withdrDTO) throws Exception;

    /**
     * 订单状态置为
     * @param withdrDTO
     */
    Map<String, Object> setSuccess(Integer adminId, AdminWithdrDTO withdrDTO);

    /**
     * 单人单日提现总额
     *
     * @param userId
     * @param addTime
     * @return
     */
    BigDecimal sumPriceByUserIdAndAddTime(Long userId, LocalDate addTime);

    /**
     * 提现申请-校验
     *
     * @param userId
     * @param price
     * @param withdrDic
     */
    void withdrApplyCheck(Long userId, BigDecimal price, WithdrDicDTO withdrDic, String bankCode);
    

}
