package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Brand;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.service.mapper.BrandGoodsMapper;
import com.zhuanbo.service.mapper.BrandMapper;
import com.zhuanbo.service.mapper.DictionaryMapper;
import com.zhuanbo.service.service.IBrandService;
import com.zhuanbo.service.vo.BrandVO;
import com.zhuanbo.service.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌商表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-26
 */
@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements IBrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private BrandGoodsMapper brandGoodsMapper;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Override
    public Object listByMobile(Integer page, Integer limit) throws Exception {
        List<Long> ids = (List<Long>) RedisUtil.get(ConstantsEnum.REDIS_BRAND_INDEX_LIST.stringValue());
        
        Map<String, Object> data = new HashMap<>();
        if (null == ids) {
        	data.put("total", 0);
            data.put("items", new ArrayList<Object>());
            return ResponseUtil.ok(data);
        }
        
        //总记录数
        int size = ids.size();
        //总页数
        int pageCount = size % limit == 0 ? size / limit : size / limit + 1;
        if (page <= 0) {
            page = 1;
        }
        int fromIndex = limit * (page - 1);
        int toIndex = fromIndex + limit ;
        if (toIndex >= size) {
            toIndex = size ;
        }
        //页数>总页数，直接返回最后一页
        List<Object> objects = null;
        if (page <= pageCount) {
            List<Long> longs = ids.subList(fromIndex, toIndex);
            List<String> keys = longs.stream().map(s -> ConstantsEnum.REDIS_BRAND_INDEX.stringValue() + s.longValue()).collect(Collectors.toList());
            objects = RedisUtil.pipelinedGet(keys);
        }

        
        if (CollectionUtils.isEmpty(objects)) {
            objects = new LinkedList<>();
        }
        data.put("total", size);
        data.put("items", objects);
        return ResponseUtil.ok(data);
    }

    @Override
    public Object detailByMobile(Long id) throws Exception {
        BrandVO vo = (BrandVO) RedisUtil.get(ConstantsEnum.REDIS_BRAND_INDEX.stringValue() + id);
        vo.setGoods(null);
        return ResponseUtil.ok(vo);
    }

    /**
     * 获取所有全部品牌id
     *
     * @return
     */
    @Override
    public List<Long> findBrandId() {
        return brandMapper.findBrandId();
    }

    /**
     * 添加品牌时把数据设置到redis
     *
     * @param brand
     */
    @Override
    public void createBrandInfoIntoRedis(Brand brand) {
        if (brand.getStatus() == 1) {
        	//实例化品牌dto
            BrandVO brandVO = new BrandVO();
            brandVO.setId(brand.getId());
            brandVO.setName(brand.getName());
            brandVO.setIndexs(brand.getIndexs());
            brandVO.setContent(brand.getContent());
            brandVO.setStarCover(brand.getStarCover());
            brandVO.setLogo(brand.getLogo());
            brandVO.setDetailCover(brand.getDetailCover());
            Integer limit = 4;
            //获取控制返回商品数据条数
            Dictionary dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("category", "brandGoods"));
            //如果字典数据不为空
            if (dictionary != null && dictionary.getLongVal() != null) {
                limit = dictionary.getLongVal().intValue();
            }
            //获取品牌关联商品信息
            List<Goods> goodsList = brandGoodsMapper.findGoodByBrandId(brand.getId(), 0, limit);
            //创建商品dto集合
            List<GoodsVo> goodDTOList = new ArrayList<>();
            //遍历查询的商品
            for (Goods good : goodsList) {
                //如果商品为空
                if (good == null) {
                    continue;
                }
                //创建返回商品对象
                GoodsVo goodInfo = new GoodsVo();
                goodInfo.setId(good.getId());
                if(good.getSideName()!=null){
                    goodInfo.setName(good.getSideName());
                }else{
                    goodInfo.setName("");
                }
                //如果图片列表不为空
                goodInfo.setCoverImages(good.getCoverImages());
                //添加数据到list
                goodDTOList.add(goodInfo);
            }
            //把商品集合添加到branddto对象中
            brandVO.setGoods(goodDTOList);
            //把数据设置到redis
            RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX + brand.getId().toString(), brandVO, Constants.CACHE_EXP_TIME);
            //获取商品ID
            List<Long> idList = brandMapper.findBrandId();
            //把id设置到缓存
            RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX_LIST.toString(), idList, Constants.CACHE_EXP_TIME);
        }
        if (brand.getStatus() == 0) {
            //删除品牌信息
            RedisUtil.del(ConstantsEnum.REDIS_BRAND_INDEX + brand.getId().toString());
            //获取商品ID
            List<Long> idList = brandMapper.findBrandId();
            //把id设置到缓存
            RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX_LIST.toString(), idList, Constants.CACHE_EXP_TIME);
        }

    }


    /**
     * 删除品牌时删除redis中的数据
     *
     * @param brand
     */
    @Override
    public void deleteBrandInfoRedis(Brand brand) {
        //删除品牌信息
        RedisUtil.del(ConstantsEnum.REDIS_BRAND_INDEX + brand.getId().toString());
        //获取商品ID
        List<Long> idList = brandMapper.findBrandId();
        //把id设置到缓存
        RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX_LIST.toString(), idList, Constants.CACHE_EXP_TIME);
    }

    /**
     * 更新品牌缓存
     */
    @Override
    public void updateRedisBrandInfo() {
        //获取品牌信息
        List<Brand> brandList = brandMapper.selectList(new QueryWrapper<Brand>().orderByAsc("id"));
        for (Brand brand : brandList) {

            if (brand.getStatus() == 1||brand.getDeleted()==0) {
                //实例化品牌dto
                BrandVO brandVO = new BrandVO();
                brandVO.setId(brand.getId());
                brandVO.setName(brand.getName());
                brandVO.setIndexs(brand.getIndexs());
                brandVO.setContent(brand.getContent());
                brandVO.setStarCover(brand.getStarCover());
                brandVO.setLogo(brand.getLogo());
                brandVO.setDetailCover(brand.getDetailCover());
                Integer limit = 4;
                //获取控制返回商品数据条数
                Dictionary dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("category", "brandGoods"));
                //如果字典数据不为空
                if (dictionary != null && dictionary.getLongVal() != null) {
                    limit = dictionary.getLongVal().intValue();
                }
                //获取品牌关联商品信息
                List<Goods> goodsList = brandGoodsMapper.findGoodByBrandId(brand.getId(), 0, limit);
                //创建商品dto集合
                List<GoodsVo> goodDTOList = new ArrayList<>();
                //遍历查询的商品
                for (Goods good : goodsList) {
                    //如果商品为空
                    if (good == null) {
                        continue;
                    }
                    //创建返回商品对象
                    GoodsVo goodInfo = new GoodsVo();
                    goodInfo.setId(good.getId());
                    if(good.getSideName()!=null){
                        goodInfo.setName(good.getSideName());
                    }else{
                        goodInfo.setName("");
                    }
                    //如果图片列表不为空
                    goodInfo.setCoverImages(good.getCoverImages());

                    //添加数据到list
                    goodDTOList.add(goodInfo);
                }
                //把商品集合添加到branddto对象中
                brandVO.setGoods(goodDTOList);
                //把数据设置到redis
                RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX + brand.getId().toString(), brandVO, Constants.CACHE_EXP_TIME);
            }

            if (brand.getStatus() == 0||brand.getDeleted()==1) {
                //删除品牌信息
                RedisUtil.del(ConstantsEnum.REDIS_BRAND_INDEX + brand.getId().toString());
            }

        }
        //获取商品ID
        List<Long> idList = brandMapper.findBrandId();
        //把id设置到缓存
        RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX_LIST.toString(), idList, Constants.CACHE_EXP_TIME);
    }

	@Override
	public void refreshRedisBrandInfo() {
		
		//获取品牌信息status=1 order by indexs desc
        List<Brand> brandList = brandMapper.selectList(new QueryWrapper<Brand>().eq("deleted", 0).eq("status", 1).orderByDesc("indexs"));
        List<Long> idList = new ArrayList<Long>();
        for (Brand brand : brandList) {

        	//实例化品牌dto
            BrandVO brandVO = new BrandVO();
            brandVO.setId(brand.getId());
            brandVO.setName(brand.getName());
            brandVO.setIndexs(brand.getIndexs());
            brandVO.setContent(brand.getContent());
            brandVO.setStarCover(brand.getStarCover());
            brandVO.setLogo(brand.getLogo());
            brandVO.setDetailCover(brand.getDetailCover());
            Integer limit = 4;
            //获取控制返回商品数据条数
            Dictionary dictionary = dictionaryMapper.selectOne(new QueryWrapper<Dictionary>().eq("category", "brandGoods"));
            //如果字典数据不为空
            if (dictionary != null && dictionary.getLongVal() != null) {
                limit = dictionary.getLongVal().intValue();
            }
            //获取品牌关联商品信息
            List<Goods> goodsList = brandGoodsMapper.findGoodByBrandId(brand.getId(), 0, limit);
            //创建商品dto集合
            List<GoodsVo> goodDTOList = new ArrayList<>();
            //遍历查询的商品
            for (Goods good : goodsList) {
                //如果商品为空
                if (good == null) {
                    continue;
                }
                //创建返回商品对象
                GoodsVo goodInfo = new GoodsVo();
                goodInfo.setId(good.getId());
                if(good.getSideName()!=null){
                    goodInfo.setName(good.getSideName());
                }else{
                    goodInfo.setName("");
                }
                //如果图片列表不为空
                goodInfo.setCoverImages(good.getCoverImages());

                //添加数据到list
                goodDTOList.add(goodInfo);
            }
            //把商品集合添加到branddto对象中
            brandVO.setGoods(goodDTOList);
            //把数据设置到redis
            RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX + brand.getId().toString(), brandVO, Constants.CACHE_EXP_TIME);

            idList.add(brand.getId());
        }
        
        //把id设置到缓存
        RedisUtil.set(ConstantsEnum.REDIS_BRAND_INDEX_LIST.toString(), idList, Constants.CACHE_EXP_TIME);
	}


}
