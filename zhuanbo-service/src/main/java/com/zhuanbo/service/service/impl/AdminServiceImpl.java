package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.service.mapper.AdminMapper;
import com.zhuanbo.service.service.IAdminService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * <p>
 * 管理员表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Autowired
    private IAdminService iAdminService;

    //放置操作者的名称，admin用户不多，放在内存就可以
    private HashMap<Integer,String> map = new HashMap<>();

    @Override
    public String getAdminName(Integer id) {
        if(StringUtils.isEmpty(map.get(id))){
            Admin admin = iAdminService.getById(id);
            if (admin == null) {
                return null;
            }
            String name = admin.getUsername();
            map.put(id,name);
            return name;
        }else{
            return map.get(id);
        }
    }
}
