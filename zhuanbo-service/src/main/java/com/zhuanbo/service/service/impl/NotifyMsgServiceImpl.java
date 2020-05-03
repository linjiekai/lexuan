package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.entity.NotifyMsg;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.mapper.NotifyMsgMapper;
import com.zhuanbo.service.service.INotifyMsgService;
import com.zhuanbo.service.service.IPushService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 * 通知消息表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
@Slf4j
public class NotifyMsgServiceImpl extends ServiceImpl<NotifyMsgMapper, NotifyMsg> implements INotifyMsgService {

    @Autowired
    private IPushService iPushService;
    @Autowired
    private IUserService iUserService;

    @Override
    public void simpleSave(User user, String platform, Integer msgFlag, String title, String content) {
        LocalDateTime now = LocalDateTime.now();
        NotifyMsg notifyMsg = new NotifyMsg();
        notifyMsg.setUserId(user.getId());
        notifyMsg.setNickname(user.getNickname());
        notifyMsg.setPlatform(platform);
        notifyMsg.setTitle(title);
        notifyMsg.setMsgFlag(msgFlag);
        notifyMsg.setReadFlag(0);
        notifyMsg.setStatus(1);
        notifyMsg.setMsgDate(DateUtil.toyyyy_MM_dd(now));
        notifyMsg.setMsgTime(DateUtil.toHH_mm_ss(now));
        notifyMsg.setContent(content);
        notifyMsg.setAddTime(now);
        notifyMsg.setUpdateTime(now);
        save(notifyMsg);
    }

    @Override
    public void notifyAndPush(User user, String platform, Integer msgFlag, String title, String content, Map<String, Object> m) {
        simpleSave(user, platform, msgFlag, title, content);
        JSONObject jsonObject = iPushService.simplePush(title, content, m, Arrays.asList(user.getId()), platform, true);
        log.info("推送结果{}", jsonObject);
    }

    @Override
    public void save(NotifyPushMQVO notifyPushMQVO) {
        User user = iUserService.getById(notifyPushMQVO.getUserId());
        notifyAndPush(user, notifyPushMQVO.getPlatform(), notifyPushMQVO.getMsgFlag(), notifyPushMQVO.getTitle(), notifyPushMQVO.getContent(), notifyPushMQVO.getExtra());
    }
}
