package com.zhuanbo.admin.api.controller;

import com.zhuanbo.client.server.client.ZhuanboLiveClient;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.dto.LiveRoomApplyDTO;
import com.zhuanbo.core.dto.ReplayVideoDTO;
import com.zhuanbo.core.dto.VideoDTO;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.service.service.IAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/live/room")
@Slf4j
public class LiveRoomController {

    @Autowired
    private ZhuanboLiveClient zhuanboLiveClient;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private AuthConfig authConfig;

    /**
     * 直播意向申请
     *
     * @return
     */
    @PostMapping("/apply")
    Object apply(@LoginAdmin Integer adminId, @RequestBody LiveRoomApplyDTO liveRoomApplyDTO) {
        log.info("|直播间申请|用户id:{}, 接收到请求报文：{}", adminId, liveRoomApplyDTO);
        Admin admin = iAdminService.getById(adminId);
        if (null != admin) {
            liveRoomApplyDTO.setOperator(admin.getUsername());
        }
        return zhuanboLiveClient.apply(liveRoomApplyDTO);
    }

    /**
     * 直播意向申请列表
     *
     * @return
     */
    @PostMapping("/page/live")
    Object pageLive(@LoginAdmin Integer adminId, @RequestBody LiveRoomApplyDTO liveRoomApplyDTO) {
        log.info("|直播意向列表|用户id:{}, 接收到请求报文：{}", adminId, liveRoomApplyDTO);
        return zhuanboLiveClient.pageLive(liveRoomApplyDTO);
    }

    /**
     * 直播意向审核
     *
     * @return
     */
    @PostMapping("/approve/live")
    Object approveLive(@LoginAdmin Integer adminId, @RequestBody LiveRoomApplyDTO liveRoomApplyDTO) {
        log.info("|直播意向审核|用户id:{}, 接收到请求报文：{}", adminId, liveRoomApplyDTO);
        Admin admin = iAdminService.getById(adminId);
        if (null != admin) {
            liveRoomApplyDTO.setOperator(admin.getUsername());
        }
        return zhuanboLiveClient.approveLive(liveRoomApplyDTO);
    }

    /**
     * 直接状态刷新
     * @param adminId
     * @return
     */
    @PostMapping("/refresh/live")
    Object refreshLive(@LoginAdmin Integer adminId) {
        return zhuanboLiveClient.refreshLive();
    }

    /**
     * 回放视频列表
     * @param adminId
     * @return
     */
    @PostMapping("/live/replay/list")
    Object liveReplayList(@LoginAdmin Integer adminId, @RequestBody ReplayVideoDTO replayVideoDTO) {
        return zhuanboLiveClient.replayVideo(replayVideoDTO);
    }

    /**
     * 回放上下架
     * @param adminId
     * @return
     */
    @PostMapping("/live/video/shelf")
    Object videoShelf(@LoginAdmin Integer adminId, @RequestBody VideoDTO videoDTO) {
        String adminName = iAdminService.getAdminName(adminId);
        videoDTO.setOperator(adminName);
        return zhuanboLiveClient.videoShelf(videoDTO);
    }

    /**
     * 直播间密码
     *
     * @return
     */
    @PostMapping("/change/password")
    Object changePassword(@LoginAdmin Integer adminId, @RequestBody LiveRoomApplyDTO liveRoomApplyDTO) {
        log.info("|直播间密码|用户id:{}, 接收到请求报文：{}", adminId, liveRoomApplyDTO);
        Admin admin = iAdminService.getById(adminId);
        if (null != admin) {
            liveRoomApplyDTO.setOperator(admin.getUsername());
        }
        return zhuanboLiveClient.changePassword(liveRoomApplyDTO);
    }

    /**
     * 回放刷新
     * @param adminId
     * @return
     */
    @PostMapping("/refresh/video")
    Object refreshVideo(@LoginAdmin Integer adminId) {
        return zhuanboLiveClient.refreshVideo();
    }
}
