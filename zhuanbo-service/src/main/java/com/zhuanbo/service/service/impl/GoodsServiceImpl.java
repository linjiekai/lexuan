package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.GoodsStatusEnum;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.dto.AdminGoodsDTO;
import com.zhuanbo.core.dto.GoodsDTO;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.Product;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.mapper.GoodsMapper;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 商品基本信息表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
@Slf4j
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Resource
    public GoodsMapper goodsMapper;
    @Autowired
    private IAdminService adminService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private AuthConfig authConfig;

    @Override
    public IPage<Goods> getPartGoods(IPage<Goods> page) {
        page.setRecords(baseMapper.getPartGoods(page));
        page.setTotal(baseMapper.getGoodsTotal());
        return page;
    }

    @Override
    public int updateBuyerNumber(Integer id, Integer number) {
        return baseMapper.updateBuyerNumber(id, number);
    }

    @Override
    public List<Goods> selectBatchIds(List<Long> ids) {
        return goodsMapper.selectBatchIds(ids);
    }

    @Override
    public Page<Goods> pageCustom(Page<Goods> page, Map<String, Object> ew) {
        page.setRecords(goodsMapper.pageCustom(page, ew));
        return page;
    }

    @Override
    public Page<Goods> page(Page<Goods> page, Map<String, Object> ew) {
        page.setRecords(goodsMapper.page(page, ew));
        return page;
    }

    @Override
    public Object generalGoodsShareList(AdminGoodsDTO dto) {
        Page<Goods> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(dto.getGoods()).ifPresent(s -> {
            Optional.ofNullable(s.getName()).ifPresent(n -> queryWrapper.like("name", n));
            Optional.ofNullable(s.getId()).ifPresent(n -> queryWrapper.eq("id", n));
        });
        queryWrapper.eq("deleted", ConstantsEnum.GOODS_DELETED_0.integerValue());// 正常的
        queryWrapper.eq("goods_type", "0");// 普通商品
        queryWrapper.orderByDesc("id");
        IPage<Goods> page = this.page(pageCond, queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", page.getTotal());
        map.put("item", page.getRecords());
        return ResponseUtil.ok(map);
    }

    @Override
    public Object updateGeneralGoodsShare(AdminGoodsDTO dto) {
        // 开启事务管理
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);

        Goods goods = dto.getGoods();
        goods.setAdminUserId(dto.getOperatorId());
        try {
            updateById(goods);
            iProductService.update(new Product(), new UpdateWrapper<Product>().set("share_price", goods.getPrice()).eq("goods_id", goods.getId()));
            txManager.commit(status);
        } catch (Exception ex) {
            txManager.rollback(status);
            log.error("GoodsServiceImpl.updateGeneralGoodsShare 失败：{}", ex);
            return ResponseUtil.serious();
        }
        return ResponseUtil.ok(getById(goods.getId()));
    }

    /**
     * 根据商品id获取商品信息
     *
     * @param goodsId
     * @return
     */
    @Override
    public GoodsDTO findGoodsDTOByGoodsId(Integer goodsId) throws Exception {
        log.info("|获取商品信息|商品id:{}", goodsId);
        Map<String, Object> params = new HashMap<>();
        params.put("id", goodsId);
        String plain = Sign.getPlain(params);
        plain += "&key=" + authConfig.getMercPrivateKey();
        log.info("plain:{}", plain);
        String sign = Sign.sign(plain);
        log.info("sign:{}", sign);
        log.info("key:{}", authConfig.getMercPrivateKey());
        Map<String, Object> headers = new HashMap<>();
        headers.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
        headers.put(ReqResEnum.X_MPMALL_SIGN.String(), sign);
        String mliveUrl = authConfig.getMliveApiUrl();
        mliveUrl = mliveUrl + "/base/goods/detail";
        String goodsStr = HttpUtil.sendPostJson(mliveUrl, params, headers);
        if (StringUtils.isBlank(goodsStr)) {
            log.error("商品获取结果为空");
            throw new ShopException(30016, "商品获取结果为空");
        }
        JSONObject retJson;
        try {
            retJson = JSON.parseObject(goodsStr);
        } catch(Exception e) {
            log.error("商品获取失败,内容解析失败");
            throw new ShopException(30017, "商品获取失败,内容解析失败");
        }
        if (!ReqResEnum.C_10000.String().equalsIgnoreCase(retJson.getString(ReqResEnum.CODE.String()))) {
            log.error("商品获取失败:{}", retJson.getString(ReqResEnum.MSG.String()));
            throw new ShopException(30021, "商品获取失败:{}" + retJson.getString(ReqResEnum.MSG.String()));
        }
        String retData = retJson.getString(ReqResEnum.DATA.String());
        return JSON.parseObject(retData, GoodsDTO.class);
    }

    @Override
    public void checkGoodsStatus(Integer goodsId) throws Exception {
        if (goodsId == null || goodsId.equals(0)) {
            return;
        }
        // 判断商品是否可以购买，商品状态[0:下架, 1：上架, 2:缺货]
        GoodsDTO goodsDTO = findGoodsDTOByGoodsId(goodsId);
        if (goodsDTO == null || goodsDTO.getId() == null || goodsDTO.getStatus() == GoodsStatusEnum.OFF_SHELVES.getId()) {
            throw new ShopException(30008);
        } else if (goodsDTO.getStatus() == GoodsStatusEnum.OUT_OF_STOCK.getId()) {
            throw new ShopException(30007);
        }
    }
}
