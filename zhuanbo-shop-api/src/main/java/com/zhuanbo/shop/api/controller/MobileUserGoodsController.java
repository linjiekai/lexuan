package com.zhuanbo.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.UserGoods;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IUserGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop/mobile/user/goods")
public class MobileUserGoodsController {

    @Autowired
    private IUserGoodsService iUserGoodsService;
    @Autowired
    private IGoodsService iGoodsService;

    /**
     * 列表
     *
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginUser Long userId) {
        return iUserGoodsService.listByUserId(userId);
    }

    /**
     * 删除
     *
     * @return
     */
    @PostMapping("/delete")
    public Object delete(@LoginUser Long userId, @RequestParam("goodsId") Long goodsId) {
        iUserGoodsService.remove(new QueryWrapper<UserGoods>().eq("user_id", userId).eq("goods_id", goodsId));
        return ResponseUtil.ok();
    }

    /**
     * 上架
     *
     * @return
     */
    @PostMapping("/putaway")
    public Object putaway(@LoginUser Long userId, @RequestParam("goodsId") Long goodsId) {
        iGoodsService.update(new Goods(),new UpdateWrapper<Goods>().eq("id",goodsId).set("status",1));
        return ResponseUtil.ok();
    }

    /**
     * 下架
     *
     * @return
     */
    @PostMapping("/soldOut")
    public Object soldOut(@LoginUser Long userId, @RequestParam("goodsId") Long goodsId) {
        iGoodsService.update(new Goods(),new UpdateWrapper<Goods>().eq("id",goodsId).set("status",0));
        return ResponseUtil.ok();
    }

}
