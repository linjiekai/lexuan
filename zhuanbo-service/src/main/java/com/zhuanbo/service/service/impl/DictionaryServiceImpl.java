package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.dto.DictionaryDTO;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.service.mapper.DictionaryMapper;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IDictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-01
 */
@Slf4j
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements IDictionaryService {

    public final String XFHL_COMPANY_ID = "xfhl_company_id";

    @Resource
    private IAdminService iAdminService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Dictionary findByCategoryAndName(String category, String name) {
        return this.getOne(new QueryWrapper<Dictionary>().eq("category", category).eq("name", name));
    }

    @Override
    public String findForString(String category, String name) {
        Dictionary dictionary = this.findByCategoryAndName(category, name);
        if (dictionary == null) {
            return null;
        }
        return dictionary.getStrVal();
    }

    @Override
    public Long findForLong(String category, String name) {
        Dictionary dictionary = this.findByCategoryAndName(category, name);
        if (dictionary == null) {
            return null;
        }
        return dictionary.getLongVal();
    }

    @Override
    public Long findCompanyIdCache() {
        return 1L;
        /*Object o = RedisUtil.get(XFHL_COMPANY_ID);
        if (o == null) {
            Dictionary one = getOne(new QueryWrapper<Dictionary>().eq("category", "mallUser").eq("name", "companyUserId"));
            RedisUtil.set(XFHL_COMPANY_ID, one.getLongVal(), 300);
            return one.getLongVal();
        }
        return Long.valueOf(o.toString());*/
    }

    @Override
    public IPage<Dictionary> pageInfo(DictionaryDTO dto) {
        Page<Dictionary> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<Dictionary> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(StringUtils.stripToNull(dto.getCategory())).ifPresent(category -> queryWrapper.eq("category", category));
        Optional.ofNullable(StringUtils.stripToNull(dto.getName())).ifPresent(name -> queryWrapper.eq("name", name));
        return this.page(pageCond, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(Integer adminId, DictionaryDTO dto) {
        Dictionary dictionary = this.getById(dto.getId());
        if (dictionary == null) {
            log.error("|字典信息更新|失败|字典id无效");
            return false;
        }
        BeanUtils.copyProperties(dto, dictionary);
        dictionary.setAdminId(adminId);
        Admin admin = iAdminService.getById(adminId);
        if (admin != null) {
            dictionary.setOperator(admin.getUsername());
        }
        return this.updateById(dictionary);
    }

    /**
     * 提现-分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public IPage<Dictionary> pageWithdr(DictionaryDTO dto) {
        Page<Dictionary> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<Dictionary> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(StringUtils.stripToNull(dto.getCategory())).ifPresent(category -> queryWrapper.likeRight("category", category));
        Optional.ofNullable(StringUtils.stripToNull(dto.getName())).ifPresent(name -> queryWrapper.eq("name", name));
        return this.page(pageCond, queryWrapper);
    }

    @Override
    public List<Dictionary> findByCategory(String category) {
        return list(new QueryWrapper<Dictionary>().eq("category", category));
    }

    @Override
    public List<Dictionary> findByCategoryCache(String category) {
        String key = RedisUtil.KEY_PRE + "Categor:" + category;
        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            return (List<Dictionary>) o;
        }
        List<Dictionary> dictionaryList = findByCategory(category);
        redisTemplate.opsForValue().set(key, dictionaryList, 1, TimeUnit.MINUTES);
        return dictionaryList;
    }
}
