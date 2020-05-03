package com.zhuanbo.client.server.client;

import com.zhuanbo.core.dto.LiveRoomApplyDTO;
import com.zhuanbo.core.dto.ReplayVideoDTO;
import com.zhuanbo.core.dto.VideoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 赚播live  client
 */
@FeignClient(name = "zhuanbo-live-api")
public interface ZhuanboLiveClient {

    /**
     * 直播意向申请
     * @return
     */
    @PostMapping("/web/live/apply")
    Object apply(@RequestBody LiveRoomApplyDTO liveRoomApplyDTO);

    /**
     * 直播意向申请列表
     *
     * @return
     */
    @PostMapping("/web/live/page/live")
    Object pageLive(@RequestBody LiveRoomApplyDTO liveRoomApplyDTO);

    /**
     * 直播意向审核
     *
     * @return
     */
    @PostMapping("/web/live/approve/live")
    Object approveLive(@RequestBody LiveRoomApplyDTO liveRoomApplyDTO);

    /**
     * 直接状态刷新
     * @return
     */
    @PostMapping("/web/live/refresh/live")
    Object refreshLive();

    /**
     * 回放视频上下架
     * @param videoDTO
     * @return
     */
    @PostMapping("/web/live/video/shelf")
    Object videoShelf(@RequestBody VideoDTO videoDTO);

    @PostMapping("/web/live/video/replay/list")
    Object replayVideo(@RequestBody ReplayVideoDTO replayVideoDTO);

    @PostMapping("/web/live/change/password")
    Object changePassword(@RequestBody LiveRoomApplyDTO liveRoomApplyDTO);

    /**
     * 回放刷新
     * @return
     */
    @PostMapping("/web/live/refresh/video")
    Object refreshVideo();
}
