package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.LinkType;
import com.zhuanbo.core.dto.AdminAdDTO;
import com.zhuanbo.core.entity.Ad;
import com.zhuanbo.service.mapper.AdMapper;
import com.zhuanbo.service.service.IAdService;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.vo.AdVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 广告表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
@Slf4j
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService {
    private final String LOCK_KEY = "adModify";
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private RedissonDistributedLocker redissonLocker;

    @Override
    public Ad getStartupPageAd(LocalDateTime now) {
        return baseMapper.getStartupPageAd(now);
    }

    @Override
    public Ad getMyAd(LocalDateTime now) {
        return baseMapper.getMyAd(now);
    }

    @Override
    public void updateAdToEffect(LocalDateTime now, Integer position) {
        baseMapper.updateAdToEffect(now, position);
    }

    @Override
    public List<AdVO> getAdList(Integer position, String platform) {
        QueryWrapper<Ad> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 2).orderByDesc("sequence_number");
        queryWrapper.eq(position != null, "position", position).eq("deleted", false);
        queryWrapper.eq("platform", platform);
        if (position == 2) {//首页广告
            queryWrapper.last("limit 6");
        } else if (position == 5) {//课时费广告
            // 不限制
        } else if (position == 4) {
            // 不限制
        } else if (position == 6) {
            // 有猫腻
            queryWrapper.last("limit 5");
        } else {
            queryWrapper.last("limit 1");
        }

        List<Ad> ads = list(queryWrapper);
        log.info("AdServiceImpl.getAdList=ads:{}", JacksonUtil.objTojson(ads));
        List<AdVO> adVOS = new ArrayList<>();

        if (ads == null || ads.size() == 0) {
            return adVOS;
        }
        for (Ad ad : ads) {
            AdVO adVO = new AdVO();
            BeanUtils.copyProperties(ad, adVO);
            if (StringUtils.isBlank(adVO.getUrlAndroid())) {
                adVO.setUrlAndroid(ad.getUrl());
            }
            if (StringUtils.isBlank(adVO.getUrlIos())) {
                adVO.setUrlIos(ad.getUrl());
            }
            adVOS.add(adVO);
        }
        log.info("AdServiceImpl.getAdList=adVOS:{}", JacksonUtil.objTojson(adVOS));
        return adVOS;
    }

    /**
     * 定时修改状态(前一天的)
     */
    @Override
    public void modifyStatus() {

        LocalDateTime localDateTime = LocalDateTime.now();
        // 未过期的
        List<Ad> adList = list(new QueryWrapper<Ad>().eq("status", 1).le("start_time", localDateTime).ge("end_time", localDateTime));
        // 按position分类
        Map<Integer, List<Ad>> adMap = adList.stream().collect(Collectors.groupingBy(Ad::getPosition));
        if (adMap != null && adMap.keySet().size() > 0) {
            // 启动页，只有一个是有效的
            if (adMap.get(1) != null) {
                List<Ad> adList1 = adMap.get(1);
                Ad ad = adList1.stream().max(Comparator.comparing(Ad::getId)).get();// 获取最大的id,(最后的优先)
                if (ad != null) {
                    ad.setStatus(2);
                    updateById(ad);
                    update(new Ad(), new UpdateWrapper<Ad>().set("status", 0).lt("id", ad.getId()));
                }
            }
            //3.个人中心只留一个
            if (adMap.get(3) != null) {
                List<Ad> adList1 = adMap.get(3);
                // 按平台分组
                Map<String, List<Ad>> pfMap = adList.stream().collect(Collectors.groupingBy(Ad::getPlatform));
                //如果名品猫个人中心广告不为空
                if (pfMap != null && pfMap.get("ZBMALL") != null) {
                    //名品玩家个人中心广告
                    List<Ad> mpList = pfMap.get("ZBMALL");
                    Ad ad = mpList.stream().max(Comparator.comparing(Ad::getId)).get();// 获取最大的id,(最后的优先)
                    if (ad != null) {
                        ad.setStatus(2);
                        updateById(ad);
                        update(new Ad(), new UpdateWrapper<Ad>().set("status", 0).lt("id", ad.getId()));
                    }

                }
                //如果名品玩家个人中心广告不为空
                if (pfMap != null && pfMap.get("MPWJMALL") != null) {
                    //名品玩家个人中心广告
                    List<Ad> mpList = pfMap.get("MPWJMALL");
                    Ad ad = mpList.stream().max(Comparator.comparing(Ad::getId)).get();// 获取最大的id,(最后的优先)
                    if (ad != null) {
                        ad.setStatus(2);
                        updateById(ad);
                        update(new Ad(), new UpdateWrapper<Ad>().set("status", 0).lt("id", ad.getId()));
                    }
                }
            }
            //2.首页有效就有效
            if (adMap.get(2) != null) {
                List<Ad> adList1 = adMap.get(2);
                if (adList1.size() > 0) {
                    update(new Ad(), new UpdateWrapper<Ad>().set("status", 2).in("id", adList1.stream().map(x -> x.getId()).collect(Collectors.toList())));
                }
            }

            //课时费
            if (adMap.get(5) != null) {
                List<Ad> adList1 = adMap.get(5);
                if (adList1.size() > 0) {
                    update(new Ad(), new UpdateWrapper<Ad>().set("status", 2).in("id", adList1.stream().map(x -> x.getId()).collect(Collectors.toList())));
                }
            }
        }
        // 有效的转过期的
        adList = list(new QueryWrapper<Ad>().select("id").eq("status", 2).lt("end_time", localDateTime));
        if (!adList.isEmpty()) {
            for (Ad ad : adList) {
                ad.setStatus(0);
                updateById(ad);
            }
        }
    }

    /**
     * 判断并保存，限制启动页广告只有1条，首页6条
     */
    public void saveByPosition(Ad ad  ,int type) {
        LocalDateTime now = LocalDateTime.now();
        Integer status = ad.getStatus();
        // 开始时间和结束时间在包含当前时间
        if ((ad.getStartTime().isBefore(now) || ad.getStartTime().isEqual(now))
                && (ad.getEndTime().isAfter(now) || ad.getEndTime().isEqual(now))) {
            // 如果状态为[待生效],则将状态置为[生效]
            status = 1 == status ? 2 : status;
            ad.setStatus(status);
        }

        switch (ad.getPosition().intValue()) {
            // 广告位置只有有一条是OK的，生效的设置成失效
            case 1:
                List<Ad> oldAdList = list(new QueryWrapper<Ad>().select("id")
                        .eq("platform", ad.getPlatform())
                        .eq("status", 2)
                        .eq("deleted", false)
                        .eq("position", 1)
                        .le("start_time", now)
                        .ge("end_time", now));
                List<Integer> ids = oldAdList.stream().map(x -> x.getId()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)) {
                    update(new Ad(), new UpdateWrapper<Ad>().set("status", 0).in("id", ids));
                }
                operationType(ad, type);
                break;
            // 首页的,只保留最新6个
            case 2:
                operationType(ad, type);
                Integer adCount = 6;
                List<Ad> adList = list(new QueryWrapper<Ad>().select("id", "sequence_number")
                        .eq("platform", ad.getPlatform())
                        .eq("status", 2)
                        .eq("deleted", false)
                        .le("start_time", now)
                        .eq("position", 2)
                        .ge("end_time", now)
                        .orderByDesc("sequence_number"));
                // 批量更新下,<6条的就不管，6条以后的置为失效
                if (!CollectionUtils.isEmpty(adList) && adList.size() > adCount) {
                    List<Integer> list = new LinkedList<>();
                    for (int i = adCount; i < adList.size(); i++) {
                        Integer id = adList.get(i).getId();
                        list.add(id);
                    }
                    update(new Ad(), new UpdateWrapper<Ad>().set("status", 0).in("id", list));
                }
                break;
            //个人中心
            case 3:
                operationType(ad, type);
                Integer centreCount = 1;
                List<Ad> centreAdList = new ArrayList<>();
                //如果为名品猫
                if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(ad.getPlatform())) {
                    centreAdList = list(new QueryWrapper<Ad>().select("id").eq("status", 2).eq("platform", "ZBMALL").eq("deleted", false).eq("position", 3).le("start_time", now).ge("end_time", now).orderByDesc("sequence_number"));
                }
                //如果为名品玩家
//                if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(ad.getPlatform())) {
//                    centreAdList = list(new QueryWrapper<Ad>().select("id").eq("status", 2).eq("platform", "MPWJMALL").eq("deleted", false).eq("position", 3).le("start_time", now).ge("end_time", now).orderByDesc("sequence_number"));
//                }

                // 批量更新下,小于 centreCount 条的就不管，centreCount 条以后的置为失效
                if (!CollectionUtils.isEmpty(centreAdList) && centreAdList.size() > centreCount) {
                    List<Integer> list = new LinkedList<>();
                    for (int i = centreCount; i < centreAdList.size(); i++) {
                        Integer id = centreAdList.get(i).getId();
                        list.add(id);
                    }
                    update(new Ad(), new UpdateWrapper<Ad>().set("status", 0).in("id", list));
                }
                break;
            case 6:
                // 有猫腻,保留5个广告
                operationType(ad, type);
                Integer mnCount = 5;
                List<Ad> mnAdList = list(new QueryWrapper<Ad>().select("id")
                        .eq("platform", ad.getPlatform())
                        .eq("status", 2)
                        .eq("deleted", false)
                        .le("start_time", now)
                        .eq("position", 6)
                        .ge("end_time", now)
                        .orderByDesc("sequence_number"));
                // 批量更新下,小于 mnCount 条的就不管，mnCount 条以后的置为失效
                if (!CollectionUtils.isEmpty(mnAdList) && mnAdList.size() > mnCount) {
                    List<Integer> list = new LinkedList<>();
                    for (int i = mnCount; i < mnAdList.size(); i++) {
                        Integer id = mnAdList.get(i).getId();
                        list.add(id);
                    }
                    update(new Ad(), new UpdateWrapper<Ad>().set("status", 0).in("id", list));
                }
                break;
            default:
                operationType(ad, type);
                break;
        }
    }

    /**
     * 根据类型操作  TODO  后期完善
     * @param ad
     * @param type
     */
    private void operationType(Ad ad,int type){
        if (type == 0) {
            save(ad);
        } else if (type == 1) {
            updateById(ad);
        }
    }

    @Override
    public void saveAds2Redis() {
        List<AdVO> adList = new LinkedList<>();
        //首页广告
        List<AdVO> indexAdList = this.getAdList(2, ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        adList.addAll(indexAdList);
        RedisUtil.set(ConstantsEnum.REDIS_INDEX_ADS.stringValue(), adList, Constants.CACHE_EXP_TIME);
        //优享广告
        List<AdVO> priorAdList = this.getAdList(4, ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        RedisUtil.set(ConstantsEnum.REDIS_PRIOR_ADS.stringValue(), priorAdList, Constants.CACHE_EXP_TIME);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object saveAds(AdminAdDTO dto) throws Exception {
        //先校验数据
        validateData(dto, 0);
        if (LinkType.TYPE_GOOD.getId().equals(dto.getType())) {
            if (dto.getLink() != null && iGoodsService.getById(Integer.parseInt(dto.getLink())) == null) {
                return ResponseUtil.fail(-1, "商品不存在");
            }
        }
        dto.setOperator(iAdminService.getAdminName(dto.getOperatorId()));
        dto.setUpdateTime(LocalDateTime.now());
        if (StringUtils.isBlank(dto.getUrl())) {
            dto.setUrl(StringUtils.isBlank(dto.getUrlAndroid()) ? dto.getUrlIos() : dto.getUrlAndroid());
        }

        boolean b = redissonLocker.tryLock(LOCK_KEY, TimeUnit.SECONDS, 5, 10);
        if (!b) {
            return ResponseUtil.result(22001);
        }

        try {
            Ad ad = new Ad();
            BeanUtils.copyProperties(dto, ad);
            saveByPosition(ad,0);
            //刷下缓存
            saveAds2Redis();
            dto.setLimit(20);
            return ResponseUtil.ok(queryAdsList(dto));
        } catch (Exception e) {
            throw new ShopException(10502);
        } finally {
            if (b) {
                redissonLocker.unlock(LOCK_KEY);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object updateAds(AdminAdDTO dto) throws Exception {
        //先校验数据
        validateData(dto, 1);
        dto.setOperator(iAdminService.getAdminName(dto.getOperatorId()));
        dto.setUpdateTime(LocalDateTime.now());
        //如果广告宣传片为空
        if (StringUtils.isBlank(dto.getUrl())) {
            dto.setUrl(StringUtils.isBlank(dto.getUrlAndroid()) ? dto.getUrlIos() : dto.getUrlAndroid());
        }

        boolean b = redissonLocker.tryLock(LOCK_KEY, TimeUnit.SECONDS, 5, 10);
        if (!b) {
            return ResponseUtil.result(22001);
        }
        try {
            Ad ad = new Ad();
            BeanUtils.copyProperties(dto, ad);
            // 修改广告信息
            LocalDateTime now = LocalDateTime.now();
            if (ad.getEndTime().isBefore(now)) {
                ad.setStatus(0);
            }
            // 更新的传1
            saveByPosition(ad ,1);
            //刷下缓存
            saveAds2Redis();
        } catch (Exception e) {
            throw new ShopException(10502);
        } finally {
            if (b) {
                redissonLocker.unlock(LOCK_KEY);
            }
        }
        dto.setLimit(20);
        return ResponseUtil.ok(queryAdsList(dto));
    }

    @Override
    public Object queryAdsList(AdminAdDTO dto) throws Exception {
        Integer position = dto.getPosition();
        String platform = dto.getPlatform();
        Page<Ad> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        //创建查询条件对象
        QueryWrapper<Ad> queryWrapper = new QueryWrapper<>();
        //根据状态降序排序
        queryWrapper.orderByDesc("status");
        queryWrapper.orderByDesc("sequence_number");
        //添加查询条件
        queryWrapper.eq("deleted", false);
        if (position != null) {
            queryWrapper.eq("position", position);
        }
        //如果品台不为空
        if (platform != null) {
            queryWrapper.eq("platform", platform);
        }
        //获取广告数据
        IPage<Ad> adIPage = this.page(pageCond, queryWrapper);
        //创建接收数据map
        Map<String, Object> data = new HashMap<>();
        //把查询的数据添加到map
        data.put("total", adIPage.getTotal());
        data.put("items", adIPage.getRecords());
        return ResponseUtil.ok(data);
    }


    /**
     * @param :[ad, type] type 0:新增，1更新
     * @return :void
     * @Description(描述):
     * @auther: Jack Lin
     * @date: 2019/8/15 13:59
     */
    private void validateData(AdminAdDTO ad, int type) throws Exception {
        if (ad == null) {
            throw new ShopException(10401);
        }
        //商品不存在
        if (LinkType.TYPE_GOOD.getId().equals(ad.getType())) {
            if (ad.getLink() != null && iGoodsService.getById(Integer.parseInt(ad.getLink())) == null) {
                throw new ShopException(30001);
            }
        }
        //序号重复
        if (ad.getPosition().intValue() != 1) {
            List<Ad> list = this.list(new QueryWrapper<Ad>().eq("sequence_number", ad.getSequenceNumber()).eq("position", ad.getPosition().intValue()).eq("deleted", false));
            if (!CollectionUtils.isEmpty(list)) {
                if (type == 0) {
                    throw new ShopException(71007);
                }
                list.stream().forEach(s -> {
                    if (s.getId().intValue() != ad.getId().intValue()) {
                        throw new ShopException(71007);
                    }
                });
            }
        }
        //过滤非法平台
        switch (ad.getPosition().intValue()) {
            case 1:
                if (!(ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(ad.getPlatform()) || ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(ad.getPlatform()))) {
                    throw new ShopException(10048);
                }
                break;
            case 3:
                if (!(ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(ad.getPlatform()) || ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(ad.getPlatform()))) {
                    throw new ShopException(10048);
                }
                break;
            case 4:
                if (!ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(ad.getPlatform())) {
                    throw new ShopException(10048);
                }
                break;
            default:
                break;
        }

    }
}
