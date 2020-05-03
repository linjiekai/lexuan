package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.BuyTypeEnum;
import com.zhuanbo.core.constants.GoodsStatusEnum;
import com.zhuanbo.core.constants.GoodsTypeEnum;
import com.zhuanbo.core.dto.GoodsDTO;
import com.zhuanbo.core.dto.MobileCartGoodsSysDTO;
import com.zhuanbo.core.dto.ProductDTO;
import com.zhuanbo.core.entity.Cart;
import com.zhuanbo.core.entity.GoodsSpecification;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.mapper.CartMapper;
import com.zhuanbo.service.service.ICartService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IProductService;
import com.zhuanbo.service.service.IUserPartnerProfitRuleService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车商品表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @Autowired
    private ICartService cartService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IUserPartnerProfitRuleService iUserPartnerProfitRuleService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private AuthConfig authConfig;

    /**
     * 添加购物车公共服务
     * @param userId
     * @param cart
     * @return
     */
    @Override
    public Object addCart(Long userId,Cart cart,boolean checked, Integer buyType) throws Exception {
        log.info("|添加购物车|用户id:{}, 接收到请求报文: {}", userId, cart);
        Integer productId = cart.getProductId();
        Integer number = cart.getNumber();
        if(!ObjectUtils.allNotNull(productId, number)){
            return ResponseUtil.badArgument();
        }
        // 购买商品数量校验[3的倍数]
        Integer goodsNumber = authConfig.getGoodsNumber();
        if (0 != number % goodsNumber) {
            log.error("商品购买数量必须为{}的倍数", goodsNumber);
            throw new ShopException(String.format("请输入商品数量为%s的倍数", authConfig.getGoodsNumber()));
        }

        // 判断sku是否失效
        ProductDTO productDTO = productService.findProductDTOById(productId);
        if (productDTO == null || productDTO.getId() == null) {
            log.error("|添加购物车|产品sku失效|用户Id:{}, 产品id:{}", userId, productId);
            return ResponseUtil.result(30006);
        }
        Integer goodsId = productDTO.getGoodsId();
        // 判断商品是否可以购买，商品状态[0:下架, 1：上架, 2:缺货]
        GoodsDTO goodsDTO = goodsService.findGoodsDTOByGoodsId(goodsId);
        log.info("商品价格|{}", goodsDTO);
        if (goodsDTO == null || goodsDTO.getId() == null || goodsDTO.getStatus() == GoodsStatusEnum.OFF_SHELVES.getId()) {
            log.error("|添加购物车|商品下架|用户Id:{}, 商品id:{}", userId, goodsDTO.getId());
            return ResponseUtil.result(30008);
        } else if (goodsDTO.getStatus() == GoodsStatusEnum.OUT_OF_STOCK.getId()) {
            log.error("|添加购物车|商品缺货|用户Id:{}, 商品id:{}", userId, goodsDTO.getId());
            return ResponseUtil.result(30007);
        }

        // 判断购物车中是否存在此规格商品,立即购买的判断立即购买的/普通购物车的判断普通购物车的：checked
        Integer cartId;
        Cart existCart = null;
        //普通购买
        if (checked) {
            existCart = cartService.getOne(new QueryWrapper<Cart>()
                    .eq("goods_id", goodsId)
                    .eq("product_id", productId)
                    .eq("user_id", userId)
                    .eq("deleted",false)
                    .eq("checked",checked));
        } else {
            // 立即购买如果存在购物车清除购物车
            cartService.remove(new QueryWrapper<Cart>()
                    .eq("goods_id", goodsId)
                    .eq("product_id", productId)
                    .eq("user_id", userId)
                    .eq("deleted",false)
                    .eq("checked", checked));
        }
        if (existCart == null) {
            cart.setGoodsId(goodsId);
            cart.setGoodsName((goodsDTO.getName()));
            cart.setGoodsType(BuyTypeEnum.BUY_TYPE_2.value().equals(buyType) ? GoodsTypeEnum.GIFT.getId() : GoodsTypeEnum.GOODS.getId());
            if (goodsDTO.getCoverImages() != null && goodsDTO.getCoverImages().size() >0) {
                cart.setPicUrl(goodsDTO.getCoverImages().get(0));
            }
            cart.setUserId(userId);
            cart.setCategoryId(goodsDTO.getCategoryId());
            // 根据用户等级获取价格
            User user = iUserService.getById(userId);
            BigDecimal price = BigDecimal.ZERO;
            if (user != null) {
                Integer ptLevel = user.getPtLevel();
                switch (ptLevel) {
                    case 0:
                        price = goodsDTO.getPlain();
                        break;
                    case 1: price = goodsDTO.getPlus();
                        break;
                    case 2: price = goodsDTO.getTrain();
                        break;
                    case 3: price = goodsDTO.getServ();
                        break;
                    case 4: price = goodsDTO.getPartner();
                        break;
                    case 5: price = goodsDTO.getDirector();
                        break;
                    default: price = goodsDTO.getPrice();
                        break;
                }
            }
            // 赠品默认是0
            cart.setPrice(BuyTypeEnum.BUY_TYPE_2.value().equals(buyType) ? BigDecimal.ZERO : price);
            List<ProductDTO.Specification> specifications = productDTO.getSpecification();
            if (specifications != null && specifications.size() > 0) {// 同引力方式
                List<Object> specificationArr = new ArrayList<>(specifications.size());
                GoodsSpecification goodsSpecification;
                for (int i = 0; i < specifications.size(); i++) {
                    ProductDTO.Specification specification = specifications.get(i);
                    if (specification != null) {
                        goodsSpecification = new GoodsSpecification();
                        goodsSpecification.setId(specification.getId());
                        goodsSpecification.setName(specification.getName());
                        specificationArr.add(goodsSpecification);
                    }
                }
                cart.setSpecifications(specificationArr);
            }
            cartService.save(cart);
            cartId = cart.getId();
        } else{
            Integer totalNum = existCart.getNumber()+number;
            if (totalNum == 0) {
                return ResponseUtil.result(30012);
            }
            existCart.setNumber(totalNum);
            cartService.updateById(existCart);
            cartId = existCart.getId();
        }
        return ResponseUtil.ok(cartId);
    }

    @Override
    public Integer calculateGoodsCount(@LoginUser Long userId) {
        int goodsCount = 0;
        List<Cart> cartList = cartService.list(new QueryWrapper<Cart>().eq("user_id",userId).eq("deleted",false));
        for(Cart cart : cartList){
            goodsCount += cart.getNumber();
        }

        return goodsCount;
    }
    
    @Override
    public Integer maxTraceType(List<Cart> checkedGoodsList) {

    	Integer maxTraceType = 0;
    	Cart cart = checkedGoodsList.stream().distinct().max(Comparator.comparing(Cart::getTraceType)).get();
        
    	if (null != cart) {
    		maxTraceType = cart.getTraceType();
    	}
        return maxTraceType;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void goodsSys(MobileCartGoodsSysDTO mobileCartGoodsSysDTO) {

        List<MobileCartGoodsSysDTO.OutProductDTO> productList = mobileCartGoodsSysDTO.getProductList();
        if (productList == null) {
            throw new ShopException("productList is null");
        }
        Map<Integer, MobileCartGoodsSysDTO.OutProductDTO> productDTOMap = productList.stream().collect(Collectors.toMap(MobileCartGoodsSysDTO.OutProductDTO::getProductId, Function.identity()));

        List<Cart> cartList = list(new QueryWrapper<Cart>().eq("goods_id", mobileCartGoodsSysDTO.getGoodsId()));
        for (Cart cart : cartList) {
            BeanUtils.copyProperties(mobileCartGoodsSysDTO, cart);
            if (productDTOMap.containsKey(cart.getProductId())) {
                BeanUtils.copyProperties(productDTOMap.get(cart.getProductId()), cart);
                updateById(cart);
            }
        }
    }

}
