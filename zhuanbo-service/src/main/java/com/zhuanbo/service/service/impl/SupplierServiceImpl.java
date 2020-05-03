package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Supplier;
import com.zhuanbo.service.mapper.SupplierMapper;
import com.zhuanbo.service.service.ISupplierService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 供应商表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-08-12
 */
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements ISupplierService {

    @Override
    public List<String> getNamesByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }
        List<Supplier> list = list(new QueryWrapper<Supplier>().select("name").in("code", codes));
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().map(x -> x.getName()).collect(Collectors.toList());
    }
}
