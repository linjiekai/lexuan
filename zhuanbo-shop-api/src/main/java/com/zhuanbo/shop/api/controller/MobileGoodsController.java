package com.zhuanbo.shop.api.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.*;
import com.zhuanbo.service.service.*;
import com.zhuanbo.shop.api.dto.req.GoodsReqDTO;
import com.zhuanbo.shop.api.dto.resp.GoodsDTO;
import com.zhuanbo.shop.api.dto.resp.ProductDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品基本信息表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/shop/mobile/goods")
public class MobileGoodsController {


    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IGoodsSpecificationService specificationService;
    @Autowired
    private IGoodsAttributeService attributeService;
    @Autowired
    private IProductService productService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IShowCategoryService iShowCategoryService;

    /**
     * 列表
     *
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody GoodsReqDTO goodsReqDTO) throws Exception {

        Page<Goods> pageCond = new Page<>(goodsReqDTO.getPage(), goodsReqDTO.getLimit());

        Map<String, Object> ew = new HashMap<>();
        ew.put("status", Arrays.asList(ConstantsEnum.GOODS_STATUS_1.integerValue(), ConstantsEnum.GOODS_STATUS_2.integerValue()));

        ew.put("goodsType", goodsReqDTO.getGoodsType());
        ew.put("buyerPartner", goodsReqDTO.getBuyerPartner());
        //展示分类ID和品牌ID二选一
        if (goodsReqDTO.getShowCategoryId() == null && goodsReqDTO.getBrandId() == null) {
            return ResponseUtil.fail(30013);
        }

        IPage<Goods> goodsIPage = null;

        if (goodsReqDTO.getShowCategoryId() != null && goodsReqDTO.getShowCategoryId().longValue() > 0) {
            List<CategoryRelation> categoryRelationList = new ArrayList<>();// 关系
            List<Long> categoryIds = new ArrayList<>();// 商品分类
            List<Long> showIds = new ArrayList<>();// 商品分类
            List<Long> goodsIds = new ArrayList<>();// 商品id
            // 分类
            if (goodsReqDTO.getShowCategoryId() != null) {
                ShowCategory showCategoryCache = iShowCategoryService.getShowCategoryCache(goodsReqDTO.getShowCategoryId());
                if (showCategoryCache != null) {
                    if (showCategoryCache.getLevel().equals(1)) {

                        List<ShowCategory> childrenList = iShowCategoryService.getChildrenCache(showCategoryCache.getId());
                        if (childrenList != null) {
                            for (ShowCategory showCategory : childrenList) {// 2级
                                List<ShowCategory> childrenList3 = showCategory.getChildrenList();// 3级
                                if (childrenList3 == null) {
                                    continue;
                                }
                                for (ShowCategory category : childrenList3) {
                                    if (category.getCategoryRelationList() == null) {
                                        continue;
                                    }
                                    categoryRelationList.addAll(category.getCategoryRelationList());
                                }
                            }
                        }
                    } else {
                        if (showCategoryCache.getCategoryRelationList() != null) {
                            categoryRelationList.addAll(showCategoryCache.getCategoryRelationList());
                        }
                    }
                }

                if (categoryRelationList.isEmpty()) {
                    return ResponseUtil.ok(MapUtil.of("total", 0, "items", Arrays.asList()));
                }
                for (CategoryRelation categoryRelation : categoryRelationList) {
                    if (categoryRelation.getCategoryId() != null && !categoryRelation.getCategoryId().equals(0L)) {
                        categoryIds.add(categoryRelation.getCategoryId());
                    }
                    if (categoryRelation.getGoodsId() != null && !categoryRelation.getGoodsId().equals(0L)) {
                        goodsIds.add(categoryRelation.getGoodsId());
                    }
                    showIds.add(categoryRelation.getShowId());
                }
            }
            ew.put("categoryIds", categoryIds);
            ew.put("goodsIds", goodsIds);
            ew.put("showIds", showIds);
            goodsIPage = goodsService.pageCustom(pageCond, ew);
        } else {
            ew.put("brandId", goodsReqDTO.getBrandId());
            switch (goodsReqDTO.getSort()) {
                case "0":
                    ew.put("sales_num","sales_num");
                    break;
                case "1":
                    ew.put("base_score","base_score");
                    break;
                case "2":
                    ew.put("priceAsc","priceAsc");
                    break;
                case "3":
                    ew.put("priceDesc","priceDesc");
                    break;
            }
            goodsIPage = goodsService.page(pageCond, ew);
        }

        List<Goods> goodsList = goodsIPage.getRecords();
        List<GoodsDTO> goodsDTOList = new ArrayList<>();
        if (goodsList != null && goodsList.size() > 0) {
            goodsList.stream().filter(good -> {
                GoodsDTO goodsDTO = new GoodsDTO();
                BeanUtils.copyProperties(good, goodsDTO);
                goodsDTO.setShareUrl(authConfig.getGoodShareUrl() + good.getId());
                if (StrUtil.isNotBlank(good.getVideoUrl())) {
                    goodsDTO.setVideoImage(good.getVideoUrl() + "?x-oss-process=video/snapshot,t_1");
                }
                if (StringUtils.isNotBlank(good.getVideoTranscodeUrl())) {// 换
                    goodsDTO.setVideoUrl(new String[]{good.getVideoTranscodeUrl()});
                }
                goodsDTOList.add(goodsDTO);
                return true;
            }).collect(Collectors.toList());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", goodsIPage.getTotal());
        data.put("items", goodsDTOList);
        return ResponseUtil.ok(data);
    }


    @PostMapping("/detail")
    public Object detail(@RequestBody GoodsReqDTO goodsReqDTO) {

        Long id = goodsReqDTO.getId();
        Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("id", id));
        if (goods == null) {
            return ResponseUtil.badResult();
        }
        GoodsDTO goodsDTO = new GoodsDTO();
        BeanUtils.copyProperties(goods, goodsDTO);
        goodsDTO.setShareUrl(authConfig.getGoodShareUrl() + goods.getId());
        //截取视频第一帧图片
        if (StrUtil.isNotBlank(goods.getVideoUrl())) {
            goodsDTO.setVideoImage(goods.getVideoUrl() + "?x-oss-process=video/snapshot,t_1");
        }
        if (StringUtils.isNotBlank(goods.getVideoTranscodeUrl())) {// 换
            goodsDTO.setVideoUrl(new String[]{goods.getVideoTranscodeUrl()});
        }

        // SKU
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<Product>().eq("goods_id", id).eq("deleted", false);
        if (goodsReqDTO.getProductId() != null) {
            productQueryWrapper.eq("id", goodsReqDTO.getProductId());
        }
        List<Product> products = productService.list(productQueryWrapper);
        List<ProductDTO> productDTOList = new ArrayList<>();
        if (products != null && products.size() > 0) {
            products.stream().filter(product -> {
                ProductDTO productDTO = new ProductDTO();
                BeanUtils.copyProperties(product, productDTO);
                if (StringUtils.isEmpty(productDTO.getUrl())) {
                    if (goodsDTO != null && goodsDTO.getCoverImages() != null && goodsDTO.getCoverImages().length > 0) {
                        productDTO.setUrl(goodsDTO.getCoverImages()[0]);
                    }
                }
                productDTOList.add(productDTO);
                return true;
            }).collect(Collectors.toList());
        }

        List<GoodsAttribute> goodsAttributes = attributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", id));
        List<GoodsSpecification> goodsSpecifications = specificationService.list(new QueryWrapper<GoodsSpecification>().eq("goods_id", id));
        List<Map<String, Object>> specifications = new ArrayList<>();
        if (goodsAttributes != null && goodsAttributes.size() > 0 && goodsSpecifications != null && goodsSpecifications.size() > 0) {
            for (GoodsAttribute a : goodsAttributes) {
                Map map = new HashMap();
                List<String> specificationValues = new ArrayList<>();
                map.put("specificationKeys", a.getName());
                for (GoodsSpecification s : goodsSpecifications) {
                    if (a.getId().equals(s.getAttributeId())) {
                        if (goodsReqDTO.getProductId() != null) {// 如果指定SKU，则只返回SKU对应的spe
                            if (Arrays.asList(products.get(0).getSpecificationIds()).contains(s.getName())) {
                                specificationValues.add(s.getName());
                            }
                        } else {
                            specificationValues.add(s.getName());
                        }
                    }
                }
                map.put("specificationValues", specificationValues);
                specifications.add(map);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("goods", goodsDTO);
        data.put("specifications", specifications);
        data.put("products", productDTOList);
        return ResponseUtil.ok(data);
    }


}
