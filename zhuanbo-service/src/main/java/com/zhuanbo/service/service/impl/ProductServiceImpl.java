package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.dto.ProductDTO;
import com.zhuanbo.core.entity.Product;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.service.mapper.ProductMapper;
import com.zhuanbo.service.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品货品表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Autowired
    private AuthConfig authConfig;

    @Override
    public boolean updateNumber(Integer productId, Integer num) {
        return baseMapper.updateNumber(productId, num) > 0;
    }

    /**
     * 调用php系统,获取商品货品信息
     *
     * @param productId
     * @return
     */
    @Override
    public ProductDTO findProductDTOById(Integer productId) {
        log.info("|获取商品product信息|productId:{}", productId);
        StringBuffer productParams = new StringBuffer();
        productParams.append("id=").append(productId);
        String mliveAdminUrl = authConfig.getMliveAdminUrl();
        mliveAdminUrl = mliveAdminUrl + "/internal/goods/getProduct";
        String productStr = HttpUtil.sendGet(mliveAdminUrl, productParams.toString());
        log.info("|获取商品product返回信息|{}", productStr);
        if (StringUtils.isBlank(productStr)) {
            log.error("product获取结果为空");
            throw new ShopException(30018, "product获取结果为空");
        }
        JSONObject retJson;
        try {
            retJson = JSON.parseObject(productStr);
        } catch(Exception e) {
            log.error("product获取失败,内容解析失败");
            throw new ShopException(30019, "product获取失败,内容解析失败");
        }
        if (!ReqResEnum.C_10000.String().equalsIgnoreCase(retJson.getString(ReqResEnum.CODE.String()))) {
            log.error("product获取失败:{}", retJson.getString(ReqResEnum.MSG.String()));
            throw new ShopException(30020, "product获取失败:{}" + retJson.getString(ReqResEnum.MSG.String()));
        }
        String retData = retJson.getString(ReqResEnum.DATA.String());
        return JSON.parseObject(retData, ProductDTO.class);
    }
}
