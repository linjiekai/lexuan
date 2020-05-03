package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminGoodsDTO;
import com.zhuanbo.core.dto.GoodsDTO;
import com.zhuanbo.core.entity.Goods;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品基本信息表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IGoodsService extends IService<Goods> {

    IPage<Goods> getPartGoods(IPage<Goods> page);

    /**
     * 更新购买人数量
     * @param id
     * @param number
     * @return
     */
    int updateBuyerNumber(Integer id, Integer number);
    /**
     * @Description(描述): 批量查询商品
     * @auther: Jack Lin
     * @param :[ids]
     * @return :java.util.List<com.zhuanbo.core.entity.Goods>
     * @date: 2019/7/5 11:06
     */
    List<Goods> selectBatchIds(List<Long> ids);

    /**
     * 自定义条件分页搜索
     * @param page
     * @param ew
     * @return
     */
    Page<Goods> pageCustom(Page<Goods> page, Map<String, Object> ew);
    
    /**
     * 查询商品
     * @param page
     * @param ew
     * @return
     */
    Page<Goods> page(Page<Goods> page, Map<String, Object> ew);

    /**
     * @Description(描述):普通商品分润列表
     * @auther: Jack Lin
     * @param :
     * @return :
     * @date: 2019/9/3 10:38
     */
    Object generalGoodsShareList(AdminGoodsDTO dto);

    /**
     * @Description(描述): 普通商品分润设置
     * @auther: Jack Lin
     * @param :[dto]
     * @return :java.lang.Object
     * @date: 2019/9/3 11:05
     */
    Object updateGeneralGoodsShare(AdminGoodsDTO dto);

    /**
     * 根据商品id获取商品信息
     * @param goodsId
     * @return
     */
    GoodsDTO findGoodsDTOByGoodsId(Integer goodsId) throws Exception;

    /**
     * 校验商品状态
     * @param goodsId
     */
    void checkGoodsStatus(Integer goodsId) throws Exception;
}
