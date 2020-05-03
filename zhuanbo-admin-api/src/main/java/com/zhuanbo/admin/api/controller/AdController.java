package com.zhuanbo.admin.api.controller;


import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.dto.AdminAdDTO;
import com.zhuanbo.core.entity.Ad;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdService;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jack Lin
 * @description: 广告管理
 * @date 2019/8/14 11:55
 */
@RestController
@RequestMapping("/admin/ad")
public class AdController {


    @Autowired
    private IAdService adService;
    @Autowired
    private IAdminService iAdminService;

    /**
     * 列表
     * 生效中：广告上线并正在首页展示--（当同一时间存在多个广告生效时，以创建时间最早的为准）---针对启动页和个人中心，position =1、3
     * 待生效：广告上线，但尚未到开始时间
     * 下线：广告已过结束时间，或者被人工操作下线
     *
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/list")
    public Object listApi(@LoginAdmin Integer adminId,
                          Integer position,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer limit, String platform) throws Exception{
        AdminAdDTO dto = new AdminAdDTO();
        dto.setOperatorId(adminId);
        dto.setPosition(position);
        dto.setPlatform(platform);
        return adService.queryAdsList(dto);
    }

    /**
     * 增
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody AdminAdDTO dto) throws Exception {
        LogOperateUtil.log("广告管理", "添加", null, adminId.longValue(), 0);
        dto.setOperatorId(adminId);
        return adService.saveAds(dto);
    }


    /**
     * 删
     *
     * @return
     */
    @PostMapping("/delete")
    public Object delete(@LoginAdmin Integer adminId, @RequestBody Ad ad) throws Exception{
        LogOperateUtil.log("广告管理", "删除", String.valueOf(ad.getId()), adminId.longValue(), 0);
        if (ad == null) {
            return ResponseUtil.badResult();
        }
        ad.setOperator(iAdminService.getAdminName(adminId));
        ad.setDeleted(true);
        adService.updateById(ad);
        return ResponseUtil.ok(listApi(adminId, ad.getPosition(), 1, 20, ad.getPlatform()));
    }

    /**
     * 查
     *
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        Ad ad = adService.getById(id);
        if (ad == null) {
            return ResponseUtil.badResult();
        } else {
            return ResponseUtil.ok(ad);
        }
    }

    /**
     * 改
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminAdDTO dto) throws Exception {
        LogOperateUtil.log("广告管理", "修改", String.valueOf(dto.getId()), adminId.longValue(), 0);
        dto.setOperatorId(adminId);
        return adService.updateAds(dto);
    }

}
