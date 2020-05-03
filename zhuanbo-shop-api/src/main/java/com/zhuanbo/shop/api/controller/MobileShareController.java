package com.zhuanbo.shop.api.controller;


import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.SpringContextUtil;
import com.zhuanbo.external.service.dto.AppIdKeyDTO;
import com.zhuanbo.external.service.wx.service.IThirdService;
import com.zhuanbo.external.service.wx.vo.ShareTicketThirdVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信jsapi分享
 */
@RestController
@RequestMapping("/shop/mobile/share")
public class MobileShareController {

    /**
     * 列表
     * @return
     */
    @PostMapping("/jsapi/ticket")
    public Object list(@RequestBody AppIdKeyDTO appIdKeyDTO) {

    	IThirdService iThirdService = (IThirdService) SpringContextUtil.getBean("weixinThirdService");
    	
        ShareTicketThirdVO wxShareTicketVO = iThirdService.getShareTicketVO(appIdKeyDTO, appIdKeyDTO.getUrl());
        if(wxShareTicketVO == null){
           return ResponseUtil.result(10042);
        }

        return ResponseUtil.ok(wxShareTicketVO);
    }

}
