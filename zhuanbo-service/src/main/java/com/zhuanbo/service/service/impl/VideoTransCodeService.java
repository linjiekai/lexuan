package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadStreamRequest;
import com.aliyun.vod.upload.resp.UploadStreamResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.vod.model.v20170321.*;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.VideoTransCodeEnum;
import com.zhuanbo.core.entity.Dynamic;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IDynamicService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IVideoTransCodeService;
import com.zhuanbo.service.vo.TransCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class VideoTransCodeService implements IVideoTransCodeService {

    final String TITLE_DYNAMIC = "动态标题";
    final String TITLE_GOODS = "商品标题";
    final String COMPLETE_ALL_SUCC = "CompleteAllSucc";
    final String TRANSCODE_SUCCESS = "TranscodeSuccess";

    @Value("${storage.aliyun.accessKeyId}")
    private String accessKeyId;
    @Value("${storage.aliyun.accessKeySecret}")
    private String accessKeySecret;

    @Autowired
    private IDynamicService iDynamicService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    @Qualifier(value = "defaultAcsClient")
    private DefaultAcsClient defaultAcsClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private QueueConfig queueConfig;



    @Override
    public TransCodeVO transCode(TransCodeVO transCodeVO) throws Exception {

        if (VideoTransCodeEnum.DYNAMIC.value().equalsIgnoreCase(transCodeVO.getMode())) {
            transByDynamic(transCodeVO);
        } else if (VideoTransCodeEnum.GOODS.value().equalsIgnoreCase(transCodeVO.getMode())){
            transByGoods(transCodeVO);
        }
        return transCodeVO;
    }

    @Override
    public void sendTrans(Object object) {

        TransCodeVO transCodeVO = new TransCodeVO();

        if (object instanceof Dynamic) {

            Dynamic dynamic = (Dynamic) object;
            transCodeVO.setMode(VideoTransCodeEnum.DYNAMIC.value());
            transCodeVO.setId(dynamic.getId());
            transCodeVO.setVideoId(null);
            transCodeVO.setTime(dynamic.getUpdateTime());
        } else if (object instanceof Goods) {

            Goods goods = (Goods) object;
            transCodeVO.setMode(VideoTransCodeEnum.GOODS.value());
            transCodeVO.setId(goods.getId().longValue());
            transCodeVO.setVideoId(null);
            transCodeVO.setTime(goods.getUpdateTime());
        } else if (object instanceof TransCodeVO) {
            transCodeVO = (TransCodeVO) object;
        } else {
            throw new RuntimeException("不支持的转码类型");
        }
        rabbitTemplate.convertAndSend(queueConfig.getExchange(), queueConfig.getQueues().getVideoTranscode().getRoutingKey(), JSON.toJSONString(transCodeVO));
    }

    private void transByDynamic(TransCodeVO transCodeVO) throws Exception {

        Dynamic dynamic = iDynamicService.getById(transCodeVO.getId());
        if (StringUtils.isNotBlank(dynamic.getVideoTranscodeUrl())) {
            if (transCodeVO.getTime().isBefore(dynamic.getUpdateTime())) {// 这部分可能一个视频还没转完，又修改了
                transCodeVO.setResult(0);
                return;
            }
        }
        // 发起过转码
        if (StringUtils.isNotBlank(transCodeVO.getVideoId())) {
            String videoUrl = tryGetVideoUrlByVideoId(transCodeVO.getVideoId());
            if (StringUtils.isBlank(videoUrl)) {
                transCodeVO.setResult(1);
                return;
            }
            dynamic.setVideoTranscodeUrl(videoUrl);
            dynamic.setVideoId(transCodeVO.getVideoId());
            iDynamicService.update(dynamic, new UpdateWrapper<Dynamic>()
                    .eq("id", dynamic.getId()).eq("update_time", dynamic.getUpdateTime()));
            transCodeVO.setResult(0);
            log.info("转码成功:{}:{},{}", transCodeVO, videoUrl, dynamic.getVideoTranscodeUrl());
        } else {// 发起转码
            String vid = uploadFroTransCode(accessKeyId, accessKeySecret, TITLE_DYNAMIC + dynamic.getId(),
                    TITLE_DYNAMIC + dynamic.getId() + fileType(dynamic.getVideoUrl()),
                    dynamic.getVideoUrl(), iDictionaryService.findForString(VideoTransCodeEnum.KEY_VIDEO.value(), VideoTransCodeEnum.KEY_TRANSCODE.value()));
            if (StringUtils.isBlank(vid)) {
                transCodeVO.setResult(0);
                log.info("转码失败:{}", transCodeVO);
                return;
            }
            transCodeVO.setVideoId(vid);
            String s = tryGetVideoUrlByVideoId(vid);
            if (StringUtils.isBlank(s)) {// 有可能异步还没完成
                transCodeVO.setResult(1);
                return;
            }
            dynamic.setVideoTranscodeUrl(s);
            dynamic.setVideoId(vid);
            iDynamicService.update(dynamic, new UpdateWrapper<Dynamic>()
                    .eq("id", dynamic.getId()).eq("update_time", dynamic.getUpdateTime()));
            transCodeVO.setResult(0);
        }
    }

    private void transByGoods(TransCodeVO transCodeVO) throws Exception {
        Goods goods = iGoodsService.getById(transCodeVO.getId());
        if (StringUtils.isNotBlank(goods.getVideoTranscodeUrl())) {
            if (transCodeVO.getTime().isBefore(goods.getUpdateTime())) {// 这部分可能一个视频还没转完，又修改了
                transCodeVO.setResult(0);
                return;
            }
        }
        // 发起过转码
        if (StringUtils.isNotBlank(transCodeVO.getVideoId())) {
            String videoUrl = tryGetVideoUrlByVideoId(transCodeVO.getVideoId());
            if (StringUtils.isBlank(videoUrl)) {
                transCodeVO.setResult(1);
                return;
            }
            goods.setVideoTranscodeUrl(videoUrl);
            goods.setVideoId(transCodeVO.getVideoId());
            iGoodsService.update(goods, new UpdateWrapper<Goods>()
                    .eq("id", goods.getId()).eq("update_time", goods.getUpdateTime()));
            transCodeVO.setResult(0);
            log.info("转码成功:{}:{},{}", transCodeVO, videoUrl, goods.getVideoTranscodeUrl());
        } else {// 发起转码
            String vid = uploadFroTransCode(accessKeyId, accessKeySecret, TITLE_GOODS + goods.getId(),
                    TITLE_GOODS + goods.getId() + fileType(goods.getVideoUrl()),
                    goods.getVideoUrl(), iDictionaryService.findForString(VideoTransCodeEnum.KEY_VIDEO.value(), VideoTransCodeEnum.KEY_TRANSCODE.value()));
            if (StringUtils.isBlank(vid)) {
                transCodeVO.setResult(0);
                log.info("转码失败:{}", transCodeVO);
                return;
            }
            transCodeVO.setVideoId(vid);
            String s = tryGetVideoUrlByVideoId(vid);
            if (StringUtils.isBlank(s)) {// 有可能异步还没完成
                transCodeVO.setResult(1);
                return;
            }
            goods.setVideoTranscodeUrl(s);
            goods.setVideoId(vid);
            iGoodsService.update(goods, new UpdateWrapper<Goods>()
                    .eq("id", goods.getId()).eq("update_time", goods.getUpdateTime()));
            transCodeVO.setResult(0);
        }
    }

    /**
     *  发起转码
     * @param accessKeyId
     * @param accessKeySecret
     * @param title 标题
     * @param fileName 文件名
     * @param url 视频地址
     * @param templateGroupId 转码模板
     * @return 视频videoId
     * @throws Exception
     */
    private String uploadFroTransCode(String accessKeyId, String accessKeySecret,
                                           String title, String fileName, String url, String templateGroupId) throws Exception {

        UploadStreamRequest request = new UploadStreamRequest(accessKeyId, accessKeySecret, title, fileName, new URL(url).openStream());
        request.setTemplateGroupId(templateGroupId);
        UploadVideoImpl uploader = new UploadVideoImpl();
        UploadStreamResponse response = uploader.uploadStream(request);
        return response.getVideoId();
    }

    /**
     * 获取转码后的视频url
     * @param videoId
     * @param client
     * @return
     * @throws ClientException
     */
    private String getVideoUrl(String videoId, DefaultAcsClient client) {

        try {
            GetPlayInfoRequest request = new GetPlayInfoRequest();
            request.setVideoId(videoId);

            GetPlayInfoResponse response = client.getAcsResponse(request);
            List<GetPlayInfoResponse.PlayInfo> playInfoList = response.getPlayInfoList();
            GetPlayInfoResponse.PlayInfo playInfo1 = playInfoList.get(0);
            return playInfo1.getPlayURL();
        } catch (ClientException e) {
            log.error("获取转码url失败:{}", e);
        }
        return null;
    }

    /**
     * 获取文件尾缀名
     * @param url
     * @return
     */
    private String fileType(String url) {
        return url.substring(url.lastIndexOf("."));
    }

    /**
     * 转码后获取地址，由于异步转码，尝试3次
     * @param videoId
     * @return
     */
    private String tryGetVideoUrlByVideoId(String videoId) {

        String url = null;
        try {
            Thread.sleep(3000L);// 延时3秒再处理吧
            ListTranscodeTaskRequest request = new ListTranscodeTaskRequest();
            request.setVideoId(videoId);
            request.setPageSize(50);
            ListTranscodeTaskResponse response = defaultAcsClient.getAcsResponse(request);


            String transcodeTaskId = null;
            for (ListTranscodeTaskResponse.TranscodeTask transcodeTask : response.getTranscodeTaskList()) {
                if (COMPLETE_ALL_SUCC.equalsIgnoreCase(transcodeTask.getTaskStatus())) {
                    transcodeTaskId = transcodeTask.getTranscodeTaskId();
                    break;
                }
            }

            if (StringUtils.isNotBlank(transcodeTaskId)) {
                GetTranscodeTaskRequest getTranscodeTaskRequest = new GetTranscodeTaskRequest();
                getTranscodeTaskRequest.setTranscodeTaskId(transcodeTaskId);
                GetTranscodeTaskResponse getTranscodeTaskResponse = defaultAcsClient.getAcsResponse(getTranscodeTaskRequest);

                if (COMPLETE_ALL_SUCC.equalsIgnoreCase(getTranscodeTaskResponse.getTranscodeTask().getTaskStatus())) {
                    for (GetTranscodeTaskResponse.TranscodeTask.TranscodeJobInfo jobInfo : getTranscodeTaskResponse.getTranscodeTask().getTranscodeJobInfoList()) {
                        if (TRANSCODE_SUCCESS.equalsIgnoreCase(jobInfo.getTranscodeJobStatus())) {
                            url = jobInfo.getOutputFile().getOutputFileUrl();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取转码url失败:{}", e);
        }
        return url;
    }
}

