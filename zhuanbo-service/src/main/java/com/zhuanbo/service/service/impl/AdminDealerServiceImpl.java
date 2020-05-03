package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.AdminDealer;
import com.zhuanbo.service.mapper.AdminDealerMapper;
import com.zhuanbo.service.service.IAdminDealerService;
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
public class AdminDealerServiceImpl extends ServiceImpl<AdminDealerMapper, AdminDealer> implements IAdminDealerService {

    @Autowired
    private IAdminDealerService iAdminDealerService;

    //放置操作者的名称，AdminDealer用户不多，放在内存就可以
    private HashMap<Integer,String> map = new HashMap<>();

    @Override
    public String getAdminName(Integer id) {
        if(StringUtils.isEmpty(map.get(id))){
            AdminDealer AdminDealer = iAdminDealerService.getById(id);
            if (AdminDealer == null) {
                return null;
            }
            String name = AdminDealer.getUsername();
            map.put(id,name);
            return name;
        }else{
            return map.get(id);
        }
    }
}
