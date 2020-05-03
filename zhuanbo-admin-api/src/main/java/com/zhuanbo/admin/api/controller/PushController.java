package com.zhuanbo.admin.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.push.PushDTO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.config.PushConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.LinkType;
import com.zhuanbo.core.entity.Dynamic;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.Push;
import com.zhuanbo.core.entity.PushReceiver;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IDynamicService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IPushReceiverService;
import com.zhuanbo.service.service.IPushService;
import com.zhuanbo.service.service.impl.PushServiceImpl;
import com.zhuanbo.service.utils.LogOperateUtil;
import com.zhuanbo.service.vo.PushParamVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 反馈表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2019-01-14
 */
@RestController
@RequestMapping("/admin/push")
@Slf4j
public class PushController {

    final Integer TYPE_0 = 0;// 商品
    final Integer TYPE_1 = 1;// 活动链接
    final Integer TYPE_2 = 2;// 动态

    @Autowired
    private IPushService iPushService;
    @Autowired
    private IPushReceiverService iPushReceiverService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IDynamicService iDynamicService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private PushConfig pushConfig;


    /**
     * 反馈列表
     * @param
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit, String title, Long id,
                       @RequestParam(defaultValue = "ZBMALL") String platform) {

        IPage<Push> pageCond = new Page<>(page, limit);
        QueryWrapper<Push> queryWrapper = new QueryWrapper();
        queryWrapper.orderByDesc("add_time");
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        if (StringUtils.isNotBlank(platform)) {
            queryWrapper.eq("platform", platform);
        }
        IPage<Push> iPage = iPushService.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());

        List<PushDTO> list = new ArrayList<>();
        PushDTO pushDTO;
        Goods goods;
        Dynamic dynamic;
        for (Push push : iPage.getRecords()) {
            pushDTO = new PushDTO();
            BeanUtils.copyProperties(push, pushDTO);
            if (TYPE_0.equals(push.getType())) {// 商品

                goods = iGoodsService.getById(push.getTargetId());
                if (goods != null) {
                    pushDTO.setTargetContent(goods.getName());
                }
            } else if (TYPE_2.equals(push.getType())) {// 活动链接

                dynamic = iDynamicService.getById(push.getTargetId());
                if (dynamic != null) {
                    pushDTO.setTargetContent(dynamic.getContent());
                }
            }
            // 推送范围
            List<PushReceiver> pushReceiverList = iPushReceiverService.list(new QueryWrapper<PushReceiver>().eq("push_id", push.getId()));
            List<Long> userIdList = pushReceiverList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
            pushDTO.setTargetUserIds(userIdList.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
            list.add(pushDTO);
        }
        data.put("items", list);
        return ResponseUtil.ok(data);
    }

    /**
     * 推送创建
     * @param adminId
     * @param pushDTO
     * @return
     */
    @Transactional
    @PostMapping("/operate")
    public Object create(@LoginAdmin Integer adminId, @RequestBody PushDTO pushDTO){
        LogOperateUtil.log("推送管理", "创建", null, adminId.longValue(), 0);
        if (StringUtils.isBlank(pushDTO.getTitle())) {
            return ResponseUtil.fail("11111", "缺少参数：title");
        }
        if (pushDTO.getType() == null) {
            return ResponseUtil.fail("11111", "缺少参数：type");
        }
        if (pushDTO.getTargetId() == null && StringUtils.isBlank(pushDTO.getTargetUrl())) {
            return ResponseUtil.fail("11111", "缺少参数：targetId或targetUrl");
        }
        if (pushDTO.getPushTime() == null) {
            return ResponseUtil.fail("11111", "缺少参数：pushTime");
        }
        if (StringUtils.isBlank(pushDTO.getTargetUserIds())) {
            return ResponseUtil.fail("11111", "缺少参数：targetUserIds");
        }

        Push oldPush = null;
        if (pushDTO.getId() != null) {
            oldPush = iPushService.getById(pushDTO.getId());
            if (oldPush != null && oldPush.getStatus() != null) {

                if (oldPush.getStatus().equals(1)) {
                    return ResponseUtil.fail("11111", "当前记录已推送，禁止修改");
                } else if (oldPush.getStatus().equals(0) && StringUtils.isNotBlank(oldPush.getTaskId())) {// 本地数据状态未推送，查询第三方状态

                    JSONObject resultjson = iPushService.taskStatus(oldPush.getTaskId(), pushDTO.getPlatform());
                    if (ConstantsEnum.PUSH_CODE_0.stringValue().equals(resultjson.getString(ConstantsEnum.PUSH_CODE.stringValue()))
                            && resultjson.getJSONObject(ConstantsEnum.PUSH_CODE_DATA.stringValue()) != null) {

                        JSONObject datajson = resultjson.getJSONObject(ConstantsEnum.PUSH_CODE_DATA.stringValue());
                        // 0-排队中, 1-发送中，2-发送完成，3-发送失败，4-消息被撤销5-消息过期, 6-筛选结果为空，7-定时任务尚未开始处理
                        if (datajson.getJSONObject(ConstantsEnum.PUSH_ANDROID.stringValue()) != null) {// 不一定有
                            String status = datajson.getJSONObject(ConstantsEnum.PUSH_ANDROID.stringValue()).getString(ConstantsEnum.PUSH_STATUS.stringValue());
                            if (ConstantsEnum.PUSH_TASK_STATUS_2.stringValue().equals(status)) {
                                oldPush.setStatus(1);
                                iPushService.updateById(oldPush);
                                return ResponseUtil.fail("11111", "当前记录已推送，禁止修改");
                            }
                        }
                        if (datajson.getJSONObject(ConstantsEnum.PUSH_IOS.stringValue()) != null) {// 不一定有
                            String status = datajson.getJSONObject(ConstantsEnum.PUSH_IOS.stringValue()).getString(ConstantsEnum.PUSH_STATUS.stringValue());
                            if (ConstantsEnum.PUSH_TASK_STATUS_2.stringValue().equals(status)) {
                                oldPush.setStatus(1);
                                iPushService.updateById(oldPush);
                                return ResponseUtil.fail("11111", "当前记录已推送，禁止修改");
                            }
                        }
                    } else {
                        log.error("推送状态异常:{}", resultjson);
                    }
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();

        Push push = new Push();
        BeanUtils.copyProperties(pushDTO, push);
        if (!push.getStatus().equals(2)) {// 非下线
            if (now.isBefore(push.getPushTime())) {
                push.setStatus(0);// 待推送
            } else {
                push.setStatus(1);// 可以推送
                /*DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                if (now.format(dateTimeFormatter).equals(push.getPushTime().format(dateTimeFormatter))) {
                    push.setStatus(1);// 刚好可以推送
                }*/
            }
        }
        push.setTaskStatus(0);
        push.setOperator(iAdminService.getAdminName(adminId));
        push.setUpdateTime(now);
        int addOrUpdate = 0;// 0：添加、1：修改
        if (push.getId() != null) {// 修改
            iPushReceiverService.remove(new QueryWrapper<PushReceiver>().eq("push_id", push.getId()));// 删除原来的
            iPushService.updateById(push);
            addOrUpdate = 1;
        } else{// 添加
            push.setAddTime(now);
            iPushService.save(push);
        }

        String userIds = pushDTO.getTargetUserIds();
        userIds = Arrays.stream(userIds.split(",")).distinct().collect(Collectors.joining(","));// 去重
        if ("0".equals(userIds)) {// 全部
            PushReceiver pushReceiver = new PushReceiver();
            pushReceiver.setPushId(push.getId());
            pushReceiver.setUserId(0L);
            pushReceiver.setAddTime(now);
            pushReceiver.setUpdateTime(now);
            pushReceiver.setStauts(1);
            pushReceiver.setAddTime(now);
            pushReceiver.setUpdateTime(now);
            iPushReceiverService.save(pushReceiver);
        } else{
            String[] uIds = userIds.split(",");
            if (uIds.length > 0) {
                PushReceiver pushReceiver;
                for (String id : uIds) {
                    pushReceiver = new PushReceiver();
                    pushReceiver.setPushId(push.getId());
                    pushReceiver.setUserId(Long.valueOf(id));
                    pushReceiver.setAddTime(now);
                    pushReceiver.setUpdateTime(now);
                    pushReceiver.setStauts(1);
                    pushReceiver.setAddTime(now);
                    pushReceiver.setUpdateTime(now);
                    iPushReceiverService.save(pushReceiver);
                }
            }
        }
        // 推送
        PushConfig.PConfig pConfig;
        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(pushDTO.getPlatform())) {
            pConfig = pushConfig.getZbmall();
        } else {
            pConfig = pushConfig.getZblmall();
        }
        PushParamVO.PushParamVOBuilder builder = PushParamVO.builder();
        if ("0".equals(userIds)) {
            builder.type(pConfig.getTypeBroadcast());
        } else {
            String pushUids = Arrays.stream(userIds.split(",")).map(x -> PushServiceImpl.PUSH_PREFIX + x).collect(Collectors.joining(","));
            builder.type(pConfig.getTypeCustomizedcas()).aliasType(pConfig.getAliasTypeAlias()).alias(pushUids);
        }
        builder.productionMode(pConfig.getProductionMode()).title(push.getTitle());
        if (push.getType().equals(LinkType.TYPE_GOOD.getId()) || push.getType().equals(LinkType.TYPE_DYNAMIC.getId())) {// 商品或动态
            builder.extra(MapUtil.of("type", push.getType(), "link", push.getTargetId()));
        } else if (push.getType().equals(1)){
            builder.extra(MapUtil.of("type", push.getType(), "link", push.getTargetUrl()));
        }
        builder.ticker(push.getSubtitle()).text(push.getSubtitle()).subtitle(push.getSubtitle()).body("")
                .mipush(pConfig.getMipush()).miActivity(pConfig.getMiActivity())
                .startTime(push.getPushTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).url(pConfig.getUrl())
                .activity(pConfig.getActivity()).afterOpen(pConfig.getAfterOpen()).platform(pushDTO.getPlatform());

        if (addOrUpdate == 1) {// 取消之前的推送任务
            JSONObject taskjson = iPushService.taskStatus(oldPush.getTaskId(), pushDTO.getPlatform());
            if (taskjson != null && taskjson.getJSONObject("data") != null || StringUtils.isNotBlank(taskjson.getJSONObject("data").getString("status"))) {
                String status = taskjson.getJSONObject("data").getString("status");
                if ("0".equals(status) || "1".equals(status)) {// 0-排队中, 1-发送中
                    iPushService.taskCancel(push.getTaskId(), pushDTO.getPlatform());
                }
            }
        }
        JSONObject pushJSON = iPushService.push(builder.build());
        if (pushJSON != null && !"0".equals(pushJSON.getString("code")) || pushJSON.getJSONObject("data") == null
                || StringUtils.isBlank(pushJSON.getJSONObject("data").getString("taskId"))) {
            log.error("推送异常:{}", pushJSON);
            throw new ShopException(pushJSON.getString("code"), pushJSON.getString("msg"));
            //throw new RuntimeException("推送异常");
        }
        push.setTaskId(pushJSON.getJSONObject("data").getString("taskId"));
        iPushService.updateById(push);

        return list(adminId, 1, 1, null, push.getId(), pushDTO.getPlatform());
    }

    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id){
        if (id == null) {
            return ResponseUtil.fail("11111", "缺少参数:id");
        }
        Push push = iPushService.getById(id);
        if (push == null) {
            return ResponseUtil.fail("11111", "数据不存在");
        }
        PushDTO pushDTO = new PushDTO();
        BeanUtils.copyProperties(push, pushDTO);

        if (TYPE_0.equals(push.getType())) {// 商品

            Goods goods = iGoodsService.getById(push.getTargetId());
            if (goods != null) {
                pushDTO.setTargetContent(goods.getName());
            }
        } else if (TYPE_2.equals(push.getType())) {// 活动链接

            Dynamic dynamic = iDynamicService.getById(push.getTargetId());
            if (dynamic != null) {
                pushDTO.setTargetContent(dynamic.getContent());
            }
        }
        // 推送范围
        List<PushReceiver> pushReceiverList = iPushReceiverService.list(new QueryWrapper<PushReceiver>().eq("push_id", push.getId()));
        List<Long> userIdList = pushReceiverList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
        pushDTO.setTargetUserIds(userIdList.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
        return ResponseUtil.ok(pushDTO);
    }
}
