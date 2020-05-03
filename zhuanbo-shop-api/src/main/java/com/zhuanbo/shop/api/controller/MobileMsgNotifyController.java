package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.YamlUtil;
import com.zhuanbo.core.entity.NotifyMsg;
import com.zhuanbo.service.service.IDynamicService;
import com.zhuanbo.service.service.INotifyMsgService;
import com.zhuanbo.shop.api.dto.req.BaseParamsDTO;
import com.zhuanbo.shop.api.dto.resp.NotifyMsgDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告
 */
@RestController
@RequestMapping("/shop/mobile/msg/notify")
public class MobileMsgNotifyController {

    @Autowired
    private INotifyMsgService iNotifyMsgService;
    @Autowired
    private IDynamicService iDynamicService;

    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginUser Long userId, @RequestBody BaseParamsDTO baseParamsDTO) {

        IPage<NotifyMsg> iPage = new Page<>(baseParamsDTO.getPage(), baseParamsDTO.getLimit());
        QueryWrapper<NotifyMsg> queryWrapper = new QueryWrapper<NotifyMsg>().eq("user_id", userId).eq("status", 1)
                .eq("platform", baseParamsDTO.getPlatform()).orderByDesc("add_time");

        IPage<NotifyMsg> userMsgIPage = iNotifyMsgService.page(iPage, queryWrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", userMsgIPage.getTotal());

        List<NotifyMsgDTO> list = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        NotifyMsgDTO notifyMsgDTO;
        for (NotifyMsg userMsg : userMsgIPage.getRecords()) {
            notifyMsgDTO = new NotifyMsgDTO();
            BeanUtils.copyProperties(userMsg, notifyMsgDTO);
            notifyMsgDTO.setLogo(YamlUtil.get("application.yml", "notify-logo.flag-1"));
            notifyMsgDTO.setMsgFlagTitle(userMsg.getMsgFlag().equals(1) ? "系统通知": "通知");
            notifyMsgDTO.setHowLongTime(iDynamicService.toHowLongTime(userMsg.getAddTime()));
            list.add(notifyMsgDTO);
            ids.add(userMsg.getId());
        }
        map.put("items", list);
        // 同步标记为已读
        if (ids.size() > 0) {
            iNotifyMsgService.update(new NotifyMsg(), new UpdateWrapper<NotifyMsg>().set("read_flag",1).in("id", ids));
        }
        return ResponseUtil.ok(map);
    }

}
