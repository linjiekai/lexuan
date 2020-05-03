package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.brand.BrandDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.entity.Brand;
import com.zhuanbo.core.entity.BrandGoods;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IBrandGoodsService;
import com.zhuanbo.service.service.IBrandService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.utils.LogOperateUtil;
import com.zhuanbo.service.vo.BrandVO;
import com.zhuanbo.service.vo.GoodsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 品牌商表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/brand")
public class BrandController {
	
    @Autowired
    private IBrandService brandService;

    @Autowired
    private IBrandGoodsService iBrandGoodsService;

    @Autowired
    private IAdminService iAdminService;

    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private RedissonDistributedLocker redissonLocker;

    private final static String CREATE_LOCK_KEY = "create_brand";

    private final static String UPDATE_LOCK_KEY = "update_brand";


    /**
     * 列表
     * @param page
     * @param limit
     * @param brand
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,  Brand brand) {
        Page<Brand> pageCond = new Page<>(page, limit);
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        //如果查询条件不为空
        Optional.ofNullable(brand).ifPresent(x ->{
            Optional.ofNullable(x.getId()).ifPresent(n -> queryWrapper.eq("id", n));
            Optional.ofNullable(x.getName()).ifPresent(n -> queryWrapper.like("name", n));
        });
        queryWrapper.orderByDesc("indexs");
        //获取品牌数据
        IPage<Brand> adIPage = brandService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        List<BrandVO> brandDTOList = new ArrayList<>();
        //如果查询的品牌数据大于领
        if(adIPage.getRecords().size() > 0){
            BrandVO brandVO=null;
            for (Brand br:adIPage.getRecords()){
                brandVO=new BrandVO();
                //把品牌对象转化成品牌dto
                BeanUtils.copyProperties(br, brandVO);
                //获取品牌关联商品信息
                List<Goods> goodsList=iBrandGoodsService.findGoodByBrandId(br.getId(),0,50);
                //创建dto对象集合
                List<GoodsVo> goodVoList=new ArrayList<>();
                //遍历商品数组
                for(Goods good:goodsList){
                    //如果商品为空继续
                    if(good==null){
                        continue;
                    }
                    //转为dto对象
                    GoodsVo goodsVo=new GoodsVo();
                    goodsVo.setName(good.getName());
                    goodsVo.setId(good.getId());
                    //添加到集合
                    goodVoList.add(goodsVo);

                }
                brandVO.setGoods(goodVoList);
                brandDTOList.add(brandVO);
            }

        }
        data.put("items", brandDTOList);
        return ResponseUtil.ok(data);
    }

    /**
     * 添加广告获取品牌信息
     * @return
     */
    @GetMapping("/listAllBrand")
    public Object listAllBrand(){
        //创建查询条件对象
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted",0);
        queryWrapper.eq("status",1);
        queryWrapper.orderByDesc("indexs");
        //获取品牌数据
        List<Brand> brandList=brandService.list(queryWrapper);
        return ResponseUtil.ok(brandList);
    }

    /**
     * 新增品牌信息
     * @return
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody BrandDTO brandDTO) {
        LogOperateUtil.log("品牌管理", "创建", null, adminId.longValue(), 0);
        boolean lockFlag = false;
        try{
            lockFlag = redissonLocker.tryLock(CREATE_LOCK_KEY, TimeUnit.SECONDS, Constants.LOCK_WAIT_TIME, Constants.LOCK_LEASE_TIME);
            if (!lockFlag) {
                return ResponseUtil.result(30014);
            }
            //校验传过来的参数
            Map<String,Object> vilMap= (Map<String, Object>) vilidaBrand("create",brandDTO);
            //如果返回码不是10000
            if( Integer.valueOf(vilMap.get("code").toString())!=10000){
                return vilMap;
            }
            brandDTO.setOperatorId(String.valueOf(adminId));
            brandDTO.setOperator(iAdminService.getAdminName(adminId));
            Brand brand=new Brand();
            //把dto转为品牌对象
            BeanUtils.copyProperties(brandDTO,brand);
            //保存品牌信息
            brandService.save(brand);
            //处理品牌商品关联
            //dealBrandGoods("create",brandDTO,brand);
            //把数据设置到redis
            brandService.createBrandInfoIntoRedis(brand);
            return list(adminId,1,20,null);
        }catch (Exception e){
            return ResponseUtil.fail();
        }finally {
            if(lockFlag){
                redissonLocker.unlock(CREATE_LOCK_KEY);
            }
        }


    }


    /**
     * 修改品牌信息
     * @param brandDTO
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody BrandDTO brandDTO) {
        LogOperateUtil.log("品牌管理", "修改", String.valueOf(brandDTO.getId()), adminId.longValue(), 0);
        //定义锁状态
        boolean lockFlag = false;
        try{
            //加锁
            lockFlag = redissonLocker.tryLock(UPDATE_LOCK_KEY, TimeUnit.SECONDS, Constants.LOCK_WAIT_TIME, Constants.LOCK_LEASE_TIME);
            //如果加锁失败，抛异常
            if (!lockFlag) {
                throw new ShopException("修改品牌信息获取分布式失败lockFlag=" + lockFlag );
            }
            //校验传过来的参数
            Map<String,Object> vilMap= (Map<String, Object>) vilidaBrand("update",brandDTO);
            //如果返回码不是10000
            if( Integer.valueOf(vilMap.get("code").toString())!=10000){
                return vilMap;
            }
            Brand brand=new Brand();
            //把dto转为品牌对象
            BeanUtils.copyProperties(brandDTO,brand);
            brand.setOperatorId(String.valueOf(adminId));
            brand.setOperator(iAdminService.getAdminName(adminId));
            //更新品牌信息
            brandService.updateById(brand);
            //处理品牌商品关联
             //dealBrandGoods("update",brandDTO,brand);
            //把数据设置到redis
            brandService.createBrandInfoIntoRedis(brand);
            return  list(adminId,1,20,null);
        }catch (Exception e){
            return ResponseUtil.fail();
        }finally {
            if(lockFlag){
                redissonLocker.unlock(UPDATE_LOCK_KEY);
            }

        }


    }

    /**
     * 校验新增和修改品牌参数
     * @param type
     */
    public Object vilidaBrand(String type,BrandDTO brandDTO){
        //如果品牌信息为空返回提示
        if(brandDTO == null){
            return ResponseUtil.badArgument();
        }
        //品牌序列号为空返回提示
        if(brandDTO.getIndexs()==null){
            return ResponseUtil.fail(11001);
        }
        //如果不为空查询根据序号查询品牌信息
        Brand isBrand=brandService.getOne(new QueryWrapper<Brand>().eq("indexs",brandDTO.getIndexs()).eq("deleted","0"));
        //如果是新增,并且查询的品牌为空返回提示
        if("create".equals(type)&&isBrand!=null){
            return ResponseUtil.fail(11002);
        }
        //如果查询到的品牌不为空返回提示
        if("update".equals(type)&&isBrand!=null&&!brandDTO.getId().equals(isBrand.getId())){
            return ResponseUtil.fail(11002);
        }
        //如果品牌名称为空返回提示
        if(brandDTO.getName()==null){
            return ResponseUtil.fail(11003);
        }
        //如果品牌logo为空返回提示
        if(brandDTO.getLogo()==null){
            return ResponseUtil.fail(11004);
        }
        //如果明星星选图片为空返回提示
        if(brandDTO.getStarCover()==null){
            return ResponseUtil.fail(11005);
        }
        //如果品牌详情页图片为空返回提示
        if(brandDTO.getDetailCover()==null){
            return ResponseUtil.fail(11006);
        }
        //如果关联商品不为空并且存在关联商品id
        if(brandDTO.getGoodsId()!=null&&brandDTO.getGoodsId().length>0){
            //遍历商品id
            for(Integer goodId:brandDTO.getGoodsId()){
                //查询商品信息
                Goods good=iGoodsService.getOne(new QueryWrapper<Goods>().eq("id",goodId));
                //如果商品不存返回提示
                if(good==null){
                    return ResponseUtil.fail(11009);
                }
                QueryWrapper queryWrapper=new QueryWrapper<BrandGoods>();
                queryWrapper.eq("goods_id",good.getId());

                if("update".equals(type)){
                    queryWrapper.notIn("brand_id",brandDTO.getId());
                }

                //根据商品id获取品牌商品关联对象
                List<BrandGoods> brandGoodsList=iBrandGoodsService.list(queryWrapper);
                //如果集合不为空并且数据大于零
                if(brandGoodsList!=null&&brandGoodsList.size()>0){
                    for(BrandGoods  brandGoods:brandGoodsList){
                       Brand brand= brandService.getOne(new QueryWrapper<Brand>().eq("id",brandGoods.getBrandId()));
                       if(brand.getStatus()==1&&brand.getDeleted()==0){
                           return ResponseUtil.fail(11010);
                       }
                    }

                }

            }
        }
        return ResponseUtil.ok();
    }

    /**
     * 处理品牌商品关联关系
     * @param type
     * @param brandDTO
     */
    public void dealBrandGoods(String type,BrandDTO brandDTO,Brand brand){
        //如果是更新
        if("update".equals(type)){
            iBrandGoodsService.remove(new QueryWrapper<BrandGoods>().eq("brand_id",brand.getId()));
        }
        //如果关联的商品不为空
        if(brandDTO.getGoodsId()!=null&&brandDTO.getGoodsId().length>0){
            //创建接受品牌商品关联对象list
            List<BrandGoods> brandGoodsList=new ArrayList<>();
            //创建空的品牌关联对象
            BrandGoods brandGoods=null;
            //遍历商品数组
            for(Integer goodId:brandDTO.getGoodsId()){
                brandGoods=new BrandGoods();
                brandGoods.setBrandId(brand.getId());
                brandGoods.setGoodsId(goodId.longValue());
                brandGoodsList.add(brandGoods);
            }
            //保存品牌商品关联信息
            iBrandGoodsService.saveBatch(brandGoodsList);
        }
    }



    /**
     * 删除品牌信息
     * @param id
     * @return
     */
    @PostMapping("/delete/{id}")
    public Object delete(@LoginAdmin Integer adminId,@PathVariable("id") Long  id) {
        
        //如果id为空返回提示
        if(id==null){
            return ResponseUtil.fail(11008);
        }
        
        LogOperateUtil.log("品牌管理", "删除", String.valueOf(id.longValue()), adminId.longValue(), 0);
        
        //根据id获取品牌信息
        Brand brandOld = brandService.getById(id);
        //如果品牌信息为空返回错误提示
        if (brandOld == null) {
            return  ResponseUtil.badResult();
        }
        brandOld.setDeleted(1);
        brandService.updateById(brandOld);
        Brand brand=new Brand();
        brand.setId(id);
        //把调整后的数据设置redis
        brandService.deleteBrandInfoRedis(brand);
        return ResponseUtil.ok();
    }

    /**
     * 更新品牌缓存
     * @return
     */
    @PostMapping("/updateRedisBrandInfo")
    public Object updateRedisBrandInfo(){
        brandService.refreshRedisBrandInfo();
        return ResponseUtil.ok();
    }
}
