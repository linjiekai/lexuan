package com.zhuanbo.shop.api.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.annotation.ResponseLog;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.BuyTypeEnum;
import com.zhuanbo.core.dto.MobileCartGoodsSysDTO;
import com.zhuanbo.core.entity.Address;
import com.zhuanbo.core.entity.Cart;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.Product;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAddressService;
import com.zhuanbo.service.service.ICartService;
import com.zhuanbo.service.service.IGoodsCarriageService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IProductService;
import com.zhuanbo.shop.api.dto.req.CartParamsDTO;
import com.zhuanbo.shop.api.dto.resp.CartGoodsDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * <p>
 * 购物车商品表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/shop/mobile/cart")
@ResponseLog
public class MobileCartController {

    @Autowired
    private ICartService cartService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IGoodsCarriageService iGoodsCarriageService;
    @Autowired
    private AuthConfig authConfig;

    /**
     * 购物车
     *
     * @param userId 用户ID
     * @return 购物车
     */
    @PostMapping("/index")
    public Object index(@LoginUser Long userId) throws InvocationTargetException, IllegalAccessException {

        List<Cart> cartList = cartService.list(new QueryWrapper<Cart>()
                .eq("user_id",userId)
                .eq("deleted",false)
                .eq("checked",true).orderByDesc("add_time")
        );
        if(cartList == null || cartList.size()==0){
            //购物车为空是正常行为
            return ResponseUtil.ok("购物车为空",new HashMap<>());
        }
        // 转list
        List<Integer> goodIds = cartList.stream().map(emp -> emp.getGoodsId()).collect(Collectors.toList());
        List<Goods> goodsList = (List<Goods>) goodsService.listByIds(goodIds);
        List<Integer> productIds = cartList.stream().map(emp -> emp.getProductId()).collect(Collectors.toList());
        List<Product> productList  = (List<Product>) productService.listByIds(productIds);

        Integer goodsCount = 0;
        BigDecimal goodsAmount = new BigDecimal(0.00);
        List<CartGoodsDTO> cartListDTO = new ArrayList<>();
        CartGoodsDTO cartGoodsDTO = null;
        for (Cart cart : cartList) {
            goodsCount += cart.getNumber();
            goodsAmount = goodsAmount.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
            cartGoodsDTO = new CartGoodsDTO();
            BeanUtils.copyProperties(cart,cartGoodsDTO);
            for (Goods g:goodsList) {
                //`status`  '商品状态： 0：下架  1：上架 2:缺货 3: 失效 ',
                if(cart.getGoodsId().equals(g.getId()) ){
                    if(g.getStatus().intValue() == 1){
                        cartGoodsDTO.setStatus(0);
                        continue;
                    }
                    cartGoodsDTO.setStatus(g.getStatus());
                    continue;
                }
            }
            for (Product p:productList) {
                if(cart.getProductId().equals(p.getId()) && cartGoodsDTO.getStatus() == 1 && p.getStatus().intValue() == -1){
                    cartGoodsDTO.setStatus(3);
                }
            }
            cartListDTO.add(cartGoodsDTO);
        }

        Map<String, Object> cartTotal = new HashMap<>();
        cartTotal.put("goodsCount", goodsCount);
        cartTotal.put("goodsAmount", goodsAmount);
        Map<String, Object> data = new HashMap<>();
        data.put("cartList", cartListDTO);
        data.put("cartTotal", cartTotal);

        return ResponseUtil.ok(data);
    }

    /**
     * 添加商品加入购物车
     * 如果已经存在购物车货品，则添加数量；
     * 否则添加新的购物车货品项。
     *
     * @param userId 用户ID
     * @param cartParamsDTO 购物车商品信息， { goodsId: xxx, productId: xxx, number: xxx }
     * @return 加入购物车操作结果
     * 购物车商品逻辑：下架、缺货不能购买，这两种状态针对good，失效（sku被删除）不能购买，这个针对每个product
     */
    @PostMapping("/add")
    public Object add(@LoginUser Long userId, @RequestBody CartParamsDTO cartParamsDTO) throws Exception {

        //普通购买展示在购物车
        Cart cart = new Cart();
        BeanUtil.copyProperties(cartParamsDTO,cart);
        cart.setChecked(true);
        return cartService.addCart(userId,cart,true, null);
    }

    /**
     * 立即购买
     * @param userId 用户ID
     * @param cartParamsDTO 购物车商品信息， { goodsId: xxx, productId: xxx, number: xxx }
     * @return 加入购物车操作结果
     * 购物车商品逻辑：下架、缺货不能购买，这两种状态针对good，失效（sku被删除）不能购买，这个针对每个product
     */
    @PostMapping("/once/add")
    public Object onceAdd(@LoginUser Long userId, @RequestBody CartParamsDTO cartParamsDTO) throws Exception {

        //普通购买展示在购物车
        Cart cart = new Cart();
        BeanUtil.copyProperties(cartParamsDTO,cart);
        cart.setChecked(false);
        Map<String, Object> cartObject = (Map<String, Object>) cartService.addCart(userId,cart,false, cartParamsDTO.getBuyType());
        if( (Integer) cartObject.get("code") != 10000){
            return cartObject;
        }
        List<Integer> cartIds = new ArrayList<>();
        cartIds.add((Integer) cartObject.get("data"));
        return checkout(userId,cartIds, cartParamsDTO.getBuyType());
    }

    /**
     * 购物车商品勾选
     * 如果原来没有勾选，则设置勾选状态；如果商品已经勾选，则设置非勾选状态。
     *
     * @param userId 用户ID
     * @param body 购物车商品信息， { productIds: xxx }
     * @return 购物车信息
     *   成功则
     *  {
     *      errno: 0,
     *      errmsg: '成功',
     *      data: xxx
     *  }
     *   失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("/checked")
    public Object checked(@LoginUser Long userId, @RequestBody String body) throws InvocationTargetException, IllegalAccessException{

        List<Integer> productIds = JacksonUtil.parseIntegerList(body, "productIds");
        if(productIds == null){
            return ResponseUtil.badArgument();
        }

        Integer checkValue = JacksonUtil.parseInteger(body, "isChecked");
        if(checkValue == null){
            return ResponseUtil.badArgument();
        }
        Boolean isChecked = (checkValue == 1);

        Cart cart = new Cart();
        cart.setChecked(isChecked);
        cartService.update(cart,new QueryWrapper<Cart>().eq("user_id",userId).eq("deleted",false)
                .in("product_id",productIds));
        return index(userId);
    }

    /**
     * 购物车商品删除
     *
     * @param userId 用户ID
     * @param cartParamsDTO 购物车商品信息， { productIds: xxx }
     * @return 购物车信息
     */
    @PostMapping("/delete")
    public Object delete(@LoginUser Long userId, @RequestBody CartParamsDTO cartParamsDTO) throws InvocationTargetException, IllegalAccessException{

        List<Integer> cartIds = cartParamsDTO.getCartIds();
        if(cartIds == null || cartIds.size() == 0){
            return ResponseUtil.badArgument();
        }
        
        cartService.remove(new QueryWrapper<Cart>().eq("user_id",userId).in("id",cartIds));
        return ResponseUtil.ok();
    }

    /**
     * 购物车商品数量
     * 如果用户没有登录，则返回空数据。
     *
     * @param userId 用户ID
     * @return 购物车商品数量
     */
    @PostMapping("goodscount")
    public Object goodscount(@LoginUser Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("goodsCount", cartService.calculateGoodsCount(userId));
        return ResponseUtil.ok(data);
    }

    /**
     * 购物车结算

     * @param userId
     * @param cartParamsDTO
     * @return
     */
    @PostMapping("/checkout")
    public Object checkoutApi(@LoginUser Long userId, @RequestBody CartParamsDTO cartParamsDTO) {

        List<Integer> cartIds = cartParamsDTO.getCartIds();
       return checkout(userId,cartIds, cartParamsDTO.getBuyType());
    }

    public Object checkout(Long userId,List<Integer> cartIds, Integer buyType){
        Address checkedAddress = addressService.getOne(new QueryWrapper<Address>()
                .eq("user_id",userId)
                .eq("is_default",true)
                .eq("deleted",false)
                .last("LIMIT 1"));
        Map<String, Object> data = new HashMap<>();
        if (checkedAddress != null) {
            addressService.fillRegion(Arrays.asList(checkedAddress));
            data.put("checkedAddress", checkedAddress);
        }
        // 商品价格
        List<Cart> checkedGoodsList = null;
        if(cartIds != null && cartIds.size()>0) {
            checkedGoodsList = (List<Cart>) cartService.listByIds(cartIds);
        }
        if(checkedGoodsList == null || checkedGoodsList.size()==0){
            return ResponseUtil.result(40002);
        }
        BigDecimal orderTotalPrice = null;
        // 商品费用
        BigDecimal goodsTotalPrice = new BigDecimal(0.00);
        int cartProductCount = 0;// 数量
        for (Cart cart : checkedGoodsList) {
            goodsTotalPrice = goodsTotalPrice.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())).divide(new BigDecimal(authConfig.getGoodsNumber()), 2,BigDecimal.ROUND_HALF_UP));
            cartProductCount += cart.getNumber();
        }
        // 运费
        BigDecimal shipPrice = BuyTypeEnum.BUY_TYPE_2.value().equals(buyType) ? iGoodsCarriageService.calculateShipping(cartProductCount) : BigDecimal.ZERO;

        //订单总额
        orderTotalPrice = goodsTotalPrice.add(shipPrice);
        data.put("orderTotalPrice", orderTotalPrice);
        data.put("goodsTotalPrice",goodsTotalPrice);//商品
        data.put("orderShipPrice",shipPrice);//运费
        data.put("checkedGoodsList", checkedGoodsList);
        return ResponseUtil.ok(data);
    }

    /**
     * 商品修改，同步购物车的数据
     * @param mobileCartGoodsSysDTO
     * @return
     */
    @PostMapping("/goods/sys")
    public Object goodsSys(@RequestBody MobileCartGoodsSysDTO mobileCartGoodsSysDTO) {
        cartService.goodsSys(mobileCartGoodsSysDTO);
        return ResponseUtil.ok();
    }
}
