package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.client.server.client.PayClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.DictionaryVisibleEnum;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.dto.PayDictionaryDTO;
import com.zhuanbo.core.dto.WithdrDicDTO;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IPayDictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Administrator
 * @title: PayDictionaryServiceImpl
 * @date 2020/4/1 11:16
 */
@Service
@Slf4j
public class PayDictionaryServiceImpl implements IPayDictionaryService {

    @Resource
    private PayClient payClient;
    @Resource
    private IAdminService iAdminService;
    @Resource
    private AuthConfig authConfig;

    /**
     * mppay 字典表数据
     *
     * @param payDictionaryDTO
     * @return
     */
    @Override
    public List<PayDictionaryDTO> list(PayDictionaryDTO payDictionaryDTO) {
        log.info("|mppay字典表数据|列表|请求报文:{}", payDictionaryDTO);
        payDictionaryDTO.setMercId(authConfig.getMercId());
        ResponseDTO responseDTO = payClient.dictionaryList(payDictionaryDTO);
        if (responseDTO == null) {
            log.error("|mppay字典表数据|列表|获取失败|请求报文:{}", payDictionaryDTO);
            return null;
        }
        if (!ReqResEnum.C_10000.String().equals(responseDTO.getCode())) {
            log.error("|mppay字典表数据|列表|获取失败:{}|请求报文:{}", responseDTO.getMsg(), payDictionaryDTO);
            return null;
        }
        List<LinkedHashMap<String, Object>> payDicMap = (List<LinkedHashMap<String, Object>>) responseDTO.getData();
        List<PayDictionaryDTO> retDicList = new ArrayList<>();
        for (LinkedHashMap<String, Object> dictionaryDTO : payDicMap) {
            String dicStr = JSONObject.toJSONString(dictionaryDTO);
            JSONObject dicJson = JSONObject.parseObject(dicStr);
            retDicList.add(JSONObject.toJavaObject(dicJson, PayDictionaryDTO.class));
        }
        return retDicList;
    }

    /**
     * mppay 字典表分页查询
     *
     * @param payDictionaryDTO
     * @return
     */
    @Override
    public ResponseDTO page(PayDictionaryDTO payDictionaryDTO) {
        log.info("|mppay字典表数据|分页|请求报文:{}", payDictionaryDTO);
        payDictionaryDTO.setVisible(DictionaryVisibleEnum.VISIBLE.getId());
        payDictionaryDTO.setMercId(authConfig.getMercId());
        ResponseDTO responseDTO = payClient.dictionaryPage(payDictionaryDTO);
        if (responseDTO == null) {
            log.error("|mppay字典表数据|列表|获取失败|请求报文:{}", payDictionaryDTO);
            throw new ShopException(11115, "获取远程字典数据失败");
        }
        return responseDTO;
    }

    /**
     * mppay 字典表分页查询
     *
     * @param payDictionaryDTO
     * @return
     */
    @Override
    public ResponseDTO update(PayDictionaryDTO payDictionaryDTO) {
        log.info("|mppay字典表数据|分页|请求报文:{}", payDictionaryDTO);
        Integer adminId = payDictionaryDTO.getAdminId();
        Admin admin = iAdminService.getById(adminId);
        if (admin != null) {
            payDictionaryDTO.setOperator(admin.getUsername());
        }
        ResponseDTO responseDTO = payClient.dictionaryUpdate(payDictionaryDTO);
        if (responseDTO == null) {
            log.error("|mppay字典表数据|列表|获取失败|请求报文:{}", payDictionaryDTO);
            throw new ShopException(11115, "获取远程字典数据失败");
        }
        return responseDTO;
    }

    /**
     * 获取提现字典数据
     *
     * @return
     */
    @Override
    public WithdrDicDTO getWithdrDic() {
        log.info("|mppay字典表数据|提现字典数据获取|");
        List<PayDictionaryDTO> withdrDicList = this.list(new PayDictionaryDTO("withdr"));
        if (null == withdrDicList || withdrDicList.size() < 1) {
            return null;
        }

        WithdrDicDTO withdrDicDTO = new WithdrDicDTO();
        for (PayDictionaryDTO payDictionaryDTO : withdrDicList) {
            String name = payDictionaryDTO.getName();
            switch (name) {
                case "bank_open":
                    withdrDicDTO.setBankOpen(payDictionaryDTO.getLongVal());
                    break;
                case "commision_ratio":
                    withdrDicDTO.setCommisionRatio(payDictionaryDTO.getLongVal());
                    break;
                case "price_max":
                    withdrDicDTO.setPriceMax(BigDecimal.valueOf(payDictionaryDTO.getLongVal()));
                    break;
                case "price_min":
                    withdrDicDTO.setPriceMin(new BigDecimal(payDictionaryDTO.getStrVal()));
                    break;
                case "times_max":
                    withdrDicDTO.setTimesMax(payDictionaryDTO.getLongVal());
                    break;
                case "to_bank_open":
                    withdrDicDTO.setToBankOpen(payDictionaryDTO.getLongVal());
                    break;
                case "to_weixin_open":
                    withdrDicDTO.setToWeixinOpen(payDictionaryDTO.getLongVal());
                    break;
                case "platform_day_limit":
                    withdrDicDTO.setPlatformDayLimit(BigDecimal.valueOf(payDictionaryDTO.getLongVal()));
                    break;
                case "person_day_limit":
                    withdrDicDTO.setPersonDayLimit(BigDecimal.valueOf(payDictionaryDTO.getLongVal()));
                    break;
                case "person_single_limit":
                    withdrDicDTO.setPersonSingleLimit(BigDecimal.valueOf(payDictionaryDTO.getLongVal()));
                    break;
                default:
                    break;
            }

        }
        return withdrDicDTO;
    }

}
