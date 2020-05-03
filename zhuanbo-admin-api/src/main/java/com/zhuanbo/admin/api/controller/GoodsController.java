package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhuanbo.admin.api.dto.goods.AttributeDTO;
import com.zhuanbo.admin.api.dto.goods.GoodsListDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.dto.AdminGoodsAttributeDTO;
import com.zhuanbo.core.dto.AdminGoodsDTO;
import com.zhuanbo.core.entity.*;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.validator.Sort;
import com.zhuanbo.service.service.*;
import com.zhuanbo.service.vo.BrandVO;
import com.zhuanbo.service.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品基本信息表 前端控制器
 * </p>
 * tokenId已经失效
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/mpmall/admin/goods")
@Slf4j
public class GoodsController {

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IGoodsSpecificationService specificationService;
    @Autowired
    private IGoodsAttributeService attributeService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private IBrandGoodsService iBrandGoodsService;
    @Autowired
    private IBrandService iBrandService;
    @Autowired
    private ICategoryService iCategoryService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    @Autowired
    private IVideoTransCodeService iVideoTransCodeService;


    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       String goodsSn, String name, Integer goodsId, Integer status, Integer goodsType, Integer buyerPartner,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort) {

        Page<Goods> pageCond = new Page<>(page, limit);
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if (goodsId != null) queryWrapper.eq("id", goodsId);
        if (StringUtils.isNotBlank(goodsSn)) queryWrapper.eq("goods_sn", goodsSn);
        if (StringUtils.isNotBlank(name)) queryWrapper.like("name", name);
        if (status != null) queryWrapper.eq("status", status);
        if (goodsType != null) {
            queryWrapper.eq("goods_type", goodsType);
        }
        queryWrapper.eq("deleted", ConstantsEnum.GOODS_DELETED_0.integerValue());// 正常的
        queryWrapper.orderByDesc("id");
        if (buyerPartner != null) {
            queryWrapper.eq("buyer_partner", buyerPartner);
        }

        IPage<Goods> goodsIPage = goodsService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();

        List<GoodsListDTO> goodsListDTOList = new ArrayList<>();

        if (goodsIPage.getRecords().size() > 0) {
            List<Goods> records = goodsIPage.getRecords();

            GoodsListDTO goodsListDTO = null;
            List<GoodsAttribute> goodsAttributeList = null;
            List<GoodsSpecification> goodsSpecificationList = null;

            List<Object> specUrlList;// 规格图片URL
            List<Object> specList;// 属性规格
            StringBuffer specBuffer;// 单个属性规格，格式：xxx:aaa,bbb,ccc
            for (Goods g : records) {

                specList = new ArrayList<>();
                specUrlList = new ArrayList<>();
                goodsListDTO = new GoodsListDTO();
                BeanUtils.copyProperties(g, goodsListDTO);
                //如果品牌id不为空，获取品牌信息
                if (goodsListDTO.getBrandId() != null) {
                    Brand brand = iBrandService.getOne(new QueryWrapper<Brand>().eq("id", goodsListDTO.getBrandId()));
                    if (brand != null) {
                        goodsListDTO.setBrandName(brand.getName());
                    } else {
                        goodsListDTO.setBrandName("");
                    }
                } else {
                    goodsListDTO.setBrandName("");
                }
                //如果分类id不为空，获取分类信息
                if (goodsListDTO.getCategoryId() != null) {
                    Category category = iCategoryService.getOne(new QueryWrapper<Category>().eq("id", goodsListDTO.getCategoryId()));
                    if (category != null) {
                        goodsListDTO.setCategoryName(category.getName());
                    } else {
                        goodsListDTO.setCategoryName("");
                    }

                } else {
                    goodsListDTO.setCategoryName("");
                }
                // 属性、规格
                goodsAttributeList = attributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", g.getId()));
                if (!goodsAttributeList.isEmpty()) {

                    for (GoodsAttribute goodsAttribute : goodsAttributeList) {

                        specBuffer = new StringBuffer();
                        specBuffer.append(goodsAttribute.getName()).append(":");

                        goodsSpecificationList = specificationService.list(new QueryWrapper<GoodsSpecification>().eq("attribute_id", goodsAttribute.getId()));
                        if (!goodsSpecificationList.isEmpty()) {
                            specBuffer.append(goodsSpecificationList.stream().map(x -> x.getName()).collect(Collectors.joining(",")));
                            specUrlList.addAll(goodsSpecificationList.stream().map(x -> x.getUrl()).collect(Collectors.toList()));
                        }
                        specList.add(specBuffer.toString());
                    }
                    goodsListDTO.setSpecifications(specList);
                    goodsListDTO.setSpecificationsUrl(specUrlList);
                }
                goodsListDTOList.add(goodsListDTO);
            }
        }
        data.put("total", goodsIPage.getTotal());
        data.put("items", goodsListDTOList);

        return ResponseUtil.ok(data);
    }

    /*
     *
     * 目前商品修改的逻辑是
     * 1. 更新goods表
     * 2. 逻辑删除goods_specification、goods_attribute、product
     * 3. 添加goods_specification、goods_attribute、product
     *
     * 这里商品三个表的数据采用删除再跟新的策略是因为
     * 商品编辑页面，管理员可以添加删除商品规格、添加删除商品属性，因此这里仅仅更新表是不可能的，
     * 因此这里只能删除所有旧的数据，然后添加新的数据
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminGoodsDTO adminGoodsDTO) {
        //获取商品信息
        Goods goods = adminGoodsDTO.getGoods();
        //打印日志
        // LogOperateUtil.log("商品管理", "修改", String.valueOf(goods.getId()), adminId.longValue(), 0);
        //如果商品为空返回提示
        if (goods == null) {
            return ResponseUtil.result(30003);
        }
        if (goods.getPrice().doubleValue() < 0.01) {
            return ResponseUtil.result(30005);
        }
        //获取商品封面图片列表
        String[] coverImages = goods.getCoverImages();
        if (coverImages == null || coverImages.length <= 0) {
            return ResponseUtil.result(13110);
        }
        for (String coverImage : coverImages) {
            if (coverImage == null) {
                return ResponseUtil.result(13110);
            }
        }
        //获取商品详情图片数组
        String[] detail = goods.getDetail();
        //如果商品图片详情数组为空返回提示
        if (detail == null || detail.length <= 0) {
            //返回提示码
            return ResponseUtil.result(13111);
        }
        //如果商品图片详情数组不为空，判断数组里面是否有空值，有返回提示
        for (String det : detail) {
            //如果图片为空返回提示
            if (det == null) {
                //返回提示码
                return ResponseUtil.result(13111);
            }
        }

        Brand brand = null;
        //如果品牌id不为空，查询品牌是否存在
        /*if (goods.getBrandId() != null && goods.getBrandId() != 0) {
            brand = iBrandService.getOne(new QueryWrapper<Brand>().eq("id", goods.getBrandId()));
            if (brand == null) {
                return ResponseUtil.fail(11012);
            }
        }*/
        Goods oldGood = goodsService.getOne(new QueryWrapper<Goods>().eq("id", goods.getId()));
        // 视频是否要转码
        boolean transCode = false;
        if (StringUtils.isNotBlank(goods.getVideoUrl())&&oldGood.getVideoUrl().equals(oldGood.getVideoUrl())) {
                transCode = true;
        }

        // SKU
       /* List<Product> productList = null;
        JSONArray products = jsonObject.getJSONArray("products");
        if (!(products == null || products.size() == 0)) {
            productList = JSON.parseArray(products.toJSONString(), Product.class);
        }*/
        // 属性规格
        List<AdminGoodsAttributeDTO> attributes = adminGoodsDTO.getAttributes();
        if (attributes == null || attributes.size() == 0) {
            return ResponseUtil.result(30002);
        }
        Goods old = goodsService.getOne(new QueryWrapper<Goods>().eq("name", goods.getName()).eq("deleted", ConstantsEnum.GOODS_DELETED_0.integerValue()));
        if (old != null && old.getName().equals(goods.getName()) && (!old.getId().equals(goods.getId()))) {
            return ResponseUtil.result(30004);
        }
        if (ConstantsEnum.GOODS_STATUS_F_1.integerValue().equals(goods.getStatus())) {// 删除直接更新状态
            goodsService.update(new Goods(), new UpdateWrapper<Goods>().set("deleted", ConstantsEnum.GOODS_DELETED_1.integerValue()).set("update_time", LocalDateTime.now()).eq("id", goods.getId()));
            return ResponseUtil.ok(list(adminId, null, null, goods.getId(), null,
                    null, null, 1, 1, null));
        }
        String lockKey = ConstantsEnum.REDIS_GOODS_UPDATE_LOCK.stringValue() + goods.getId();
        boolean tryLock = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 10, 10);
        if (!tryLock) {
            return ResponseUtil.result(30014);
        }
        // 开启事务管理
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);

        LocalDateTime now = LocalDateTime.now();
        try {
            // 商品基本信息表goods
            goods.setVideoId(oldGood.getVideoId());
            goods.setVideoTranscodeUrl(oldGood.getVideoTranscodeUrl());
            goods.setUpdateTime(now);
            //goods.setOperator(iAdminService.getAdminName(adminId));
            goodsService.updateById(goods);

            // 删除原来的
            specificationService.remove(new QueryWrapper<GoodsSpecification>().eq("goods_id", goods.getId()));
            attributeService.remove(new QueryWrapper<GoodsAttribute>().eq("goods_id", goods.getId()));
            //
            makeAttrSpecProduct(goods, attributes, "update");
            //如果品牌不为空设置品牌redis信息
            /*if (brand != null) {
                iBrandService.createBrandInfoIntoRedis(brand);
                if (!brand.getId().equals(oldGood.getBrandId())) {
                    //获取旧的brand
                    Brand oldBrand = iBrandService.getOne(new QueryWrapper<Brand>().eq("id", oldGood.getBrandId()));
                    if (oldBrand != null) {
                        iBrandService.createBrandInfoIntoRedis(oldBrand);
                    }

                }
            }*/

        } catch (ShopException ex) {
            txManager.rollback(status);
            log.error("系统内部错误", ex);
            return ResponseUtil.result(Integer.valueOf(ex.getCode()));
        } catch (Exception ex) {
            txManager.rollback(status);
            log.error("系统内部错误", ex);
            return ResponseUtil.serious();
        } finally {
            if (tryLock) {
                redissonLocker.unlock(lockKey);
            }
        }
        txManager.commit(status);
        // 视频转码
        if (transCode) {
            if (StringUtils.isNotBlank(goods.getVideoUrl())) {
                String s = goods.getVideoUrl();
                if (StringUtils.isNotBlank(s)) {
                    iVideoTransCodeService.sendTrans(goods);
                }
            }
        }
        // 返回列表的单条记录
        return ResponseUtil.ok(list(adminId, null, null, goods.getId(),
                null, null, null, 1, 1, null));
    }

    /**
     * 商品添加
     *
     * @param adminId
     * @return
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody AdminGoodsDTO adminGoodsDTO) {
        // LogOperateUtil.log("商品管理", "创建", null, adminId.longValue(), 0);
        //获取商品信息
        Goods goods = adminGoodsDTO.getGoods();
        //如果商品信息为空返回提示
        if (goods == null) {
            return ResponseUtil.result(30003);
        }
        //如果商品价格为空或者价格小于0.01
        if (goods.getPrice().doubleValue() < 0.01) {
            return ResponseUtil.result(30005);
        }
        String[] coverImages = goods.getCoverImages();
        if (coverImages == null || coverImages.length <= 0) {
            return ResponseUtil.result(13110);
        }
        for (String coverImage : coverImages) {
            if (coverImage == null) {
                return ResponseUtil.result(13110);
            }
        }

        //获取商品详情图片数组
        String[] detail = goods.getDetail();
        if (detail == null || detail.length <= 0) {
            return ResponseUtil.result(13111);
        }
        for (String det : detail) {
            if (det == null) {
                return ResponseUtil.result(13111);
            }
        }

        // 属性规格
        List<AdminGoodsAttributeDTO> attributes = adminGoodsDTO.getAttributes();
        if (attributes == null || attributes.size() == 0) {
            return ResponseUtil.result(30002);
        }
        if (goodsService.count(new QueryWrapper<Goods>().eq("name", goods.getName()).eq("deleted", ConstantsEnum.GOODS_DELETED_0.integerValue())) > 0) {
            return ResponseUtil.result(30004);
        }
        Brand brand = null;
        //如果品牌id不为空，查询品牌是否存在
        /*if (goods.getBrandId() != null) {
            brand = iBrandService.getOne(new QueryWrapper<Brand>().eq("id", goods.getBrandId()));
            if (brand == null) {
                return ResponseUtil.fail(11012);
            }
        }*/

        // 开启事务管理
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);

        LocalDateTime now = LocalDateTime.now();
        try {
            // 商品基本信息表goods
            goods.setAddTime(now);
            goods.setUpdateTime(now);
            //goods.setOperator(iAdminService.getAdminName(adminId));
            goodsService.save(goods);
            //保存属性规格
            makeAttrSpecProduct(goods, attributes, "add");
            //如果品牌不为空设置品牌redis信息
            if (brand != null) {
                iBrandService.createBrandInfoIntoRedis(brand);
            }
        } catch (Exception ex) {
            txManager.rollback(status);
            log.error("系统内部错误", ex);
        }
        txManager.commit(status);
        // 视频转码
        if (StringUtils.isNotBlank(goods.getVideoUrl())) {
            String s = goods.getVideoUrl();
            if (StringUtils.isNotBlank(s)) {
                iVideoTransCodeService.sendTrans(goods);
            }
        }
        // 返回列表的单条记录
        return ResponseUtil.ok(list(adminId, null, null, goods.getId(),
                null, null, null, 1, 1, null));
    }

    @GetMapping("/detail")
    public Object detail(@LoginAdmin Integer adminId, @NotNull Integer id) {

        Goods goods = goodsService.getById(id);
        List<Product> products = productService.list(new QueryWrapper<Product>().eq("goods_id", id).eq("deleted", ConstantsEnum.PRODUCT_DELETED_0.integerValue()));
        List<GoodsSpecification> specifications = specificationService.list(new QueryWrapper<GoodsSpecification>().eq("goods_id", id));
        List<GoodsAttribute> attributes = attributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", id));

        List<AttributeDTO> attributeDTOList = new ArrayList<>();
        AttributeDTO attributeDTO = null;
        GoodsSpecification goodsSpecification = null;
        for (GoodsAttribute a : attributes) {
            attributeDTO = new AttributeDTO();
            attributeDTO.setGoodsAttribute(a);

            Iterator<GoodsSpecification> it = specifications.iterator();
            while (it.hasNext()) {
                goodsSpecification = it.next();
                if (a.getId().equals(goodsSpecification.getAttributeId())) {
                    attributeDTO.getGoodsSpecifications().add(goodsSpecification);
                    it.remove();
                }
            }
            attributeDTOList.add(attributeDTO);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("products", products);
        data.put("attributes", attributeDTOList);


        return ResponseUtil.ok(data);
    }

    /**
     * 保存属性规格、SKU
     *
     * @param goods      商品
     * @param attributes 属性规格
     * @param action     add:添加、update:删除
     */
    private void makeAttrSpecProduct(Goods goods, List<AdminGoodsAttributeDTO> attributes, String action) {
        if (goods == null || attributes == null || attributes.size() == 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        AdminGoodsAttributeDTO goodsAttribute = null;
        List<List<String>> allsSpecifications = new ArrayList<>();// 所有规格
        Map<String, String> urlMap = new HashMap<>();// 规格与图片

        for (int i = 0; i < attributes.size(); i++) {

            goodsAttribute = attributes.get(i);
            if (StringUtils.isBlank(goodsAttribute.getName())) {
                throw new ShopException(30009);
            }
            goodsAttribute.setAddTime(now);
            goodsAttribute.setGoodsId(goods.getId());
            // 保存属性
            attributeService.save(goodsAttribute);

            List<GoodsSpecification> specifications = goodsAttribute.getSpecifications();
            List<String> goodsSpecificationList = null;// 一个属性下的所有规格集合
            if (CollectionUtils.isEmpty(specifications)) {
                throw new ShopException(30011);
            }

            goodsSpecificationList = new ArrayList<>();
            GoodsSpecification goodsSpecification = null;
            for (int j = 0; j < specifications.size(); j++) {
                goodsSpecification = specifications.get(j);
                if (goodsSpecification != null) {
                    if (StringUtils.isBlank(goodsSpecification.getName())) {
                        throw new ShopException(30010);
                    }
                    goodsSpecification.setGoodsId(goods.getId());
                    goodsSpecification.setAttributeId(goodsAttribute.getId());
                    goodsSpecification.setCreateTime(now);
                    // 保存规格
                    specificationService.save(goodsSpecification);
                    goodsSpecificationList.add(goodsSpecification.getName());
                    if (StringUtils.isNotBlank(goodsSpecification.getUrl())) {
                        urlMap.put(goodsSpecification.getName(), goodsSpecification.getUrl());
                    }
                }
            }
            allsSpecifications.add(goodsSpecificationList);
        }

        // 属性规格组成SKU
        List<List<String>> allSKUSpecifications = new ArrayList<>(Lists.cartesianProduct(allsSpecifications));
        // 判断SKU是否删除还是新加的
        if ("update".equals(action)) {// 添加的直接添加， 更新的，判断当前的SKU和之前的SKU是否一致，多的新增，少的删除(软)
            List<Product> productList = productService.list(new QueryWrapper<Product>().eq("goods_id", goods.getId()).eq("deleted", ConstantsEnum.PRODUCT_DELETED_0.integerValue()));

            List<Integer> deleteIdList = new ArrayList<>();// 要删除的原先的SKU的id
            for (Product p : productList) {// 判断旧的SKU的规格和新的SKU是否有存在的，不存在就把旧的SKU删除(软)
                if (p.getSpecificationIds() == null || p.getSpecificationIds().length == 0) {
                    deleteIdList.add(p.getId());
                    continue;
                }
                boolean del = true;// 旧的sku是否要删除掉
                Iterator<List<String>> iterator = allSKUSpecifications.iterator();
                List<String> next = null;
                while (iterator.hasNext()) {
                    next = iterator.next();
                    if (next.containsAll(Arrays.asList(p.getSpecificationIds())) && next.size() == p.getSpecificationIds().length) {
                        del = false;
                        //p.setSharePrice(goods.getSharePrice());
                        //p.setGoodsType(goods.getGoodsType());
                        iterator.remove();// 删除，剩下的就是要添加的新的SKU规格
                        break;
                    }
                }
                if (del) {// 当前旧的SKU要删除
                    p.setStatus(-1);
                }
                productService.updateById(p);
            }
        }
        if (allSKUSpecifications.size() > 0) {// 添加新的SKU
            Product product;
            for (List<String> l : allSKUSpecifications) {
                product = new Product();
                product.setGoodsId(goods.getId());
                product.setCreateTime(now);
                product.setStatus(ConstantsEnum.PRODUCT_DELETED_1.integerValue());// 正常
                product.setSpecificationIds(l.stream().toArray(String[]::new));
                //product.setProfitPrice(goods.getProfitPrice());
                //product.setSharePrice(goods.getSharePrice());
                /*product.setTrainEq(goods.getTrainEq());
                product.setServEq(goods.getTrainEq());
                product.setServIndt(goods.getServIndt());
                product.setServLower(goods.getServLower());*/
                //product.setGoodsType(goods.getGoodsType());
                productService.save(product);
            }
        }
    }

    /**
     * 根据规格获取图片
     *
     * @param list
     * @param map
     * @return
     */
    private String getUrl(List<String> list, Map<String, String> map) {
        if (list == null || list.size() == 0 || map == null) {
            return "";
        } else {
            for (String key : map.keySet()) {
                if (list.contains(key)) {
                    return map.get(key);
                }
            }
        }
        return "";
    }

    /**
     * 只返回用户id和用户名
     *
     * @return
     */
    @GetMapping("/idNameList")
    public Object idNameList(@LoginAdmin Integer adminId,
                             @RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "-1") Integer limit,
                             @RequestParam(required = false) Integer id) {

        Page<Goods> pageCond = new Page<>(page, limit);
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", ConstantsEnum.GOODS_DELETED_0.integerValue());
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        IPage<Goods> iPage = goodsService.page(pageCond, queryWrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", limit.equals(-1) ? goodsService.count(queryWrapper) : iPage.getTotal());
        List<Map> list = new ArrayList<>();
        if (iPage.getRecords().size() > 0) {
            for (Goods g : iPage.getRecords()) {
                list.add(MapUtil.of("id", g.getId(), "name", g.getName()));
            }
        }
        map.put("item", list);
        return ResponseUtil.ok(map);
    }


    /**
     * 修改商品更新品牌商品关联redis中的值
     *
     * @param goods
     */
    public void dealBrandGoodInfo(Integer brandId, Goods goods) {


        //获取商品关联的信息
        List<BrandGoods> goodBrandList = iBrandGoodsService.list(new QueryWrapper<BrandGoods>().eq("goods_id", goods.getId()));
        //如果查询的品牌商品关联信息不为空
        if (goodBrandList == null) {
            return;
        }
        //遍历集合
        for (BrandGoods brandGoods : goodBrandList) {
            //获取更新的数据
            Object upObject = RedisUtil.get(ConstantsEnum.REDIS_BRAND_INDEX + brandGoods.getBrandId().toString());
            //把object转为brandVO
            BrandVO brandOld = (BrandVO) upObject;
            //如果获取的数据不为空
            if (brandOld == null) {
                return;
            }
            //品牌关联的商品
            List<GoodsVo> goodInfos = brandOld.getGoods();
            //如果获取的商品不为空，并且集合里面有数据
            if (goodInfos == null || goodInfos.size() <= 0) {
                return;
            }
            //遍历商品信息
            for (GoodsVo goodInfo : goodInfos) {
                //如果商品id等于修改商品id
                if (goodInfo.getId().equals(goods.getId())) {
                    goodInfo.setName(goods.getName());
                    if (goods.getCoverImages() != null && goods.getCoverImages().length >= 0) {
                        goodInfo.setCoverImages(goods.getCoverImages());
                    }
                }
            }
            //把值设置到redis
            RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX + brandGoods.getBrandId().toString(), brandOld);
        }

    }

    @PostMapping("/generalGoods/shareList")
    public Object generalGoodsShareList(@LoginAdmin Integer adminId, @RequestBody AdminGoodsDTO adminGoodsDTO) {
        adminGoodsDTO.setOperatorId(adminId);
        return goodsService.generalGoodsShareList(adminGoodsDTO);
    }

    @PostMapping("/generalGoods/updateShare")
    public Object updateGeneralGoodsShare(@LoginAdmin Integer adminId, @RequestBody AdminGoodsDTO adminGoodsDTO) {
        adminGoodsDTO.setOperatorId(adminId);
        return goodsService.updateGeneralGoodsShare(adminGoodsDTO);
    }

}
