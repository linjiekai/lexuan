package com.zhuanbo.admin.api.mq;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbitmq.client.Channel;
import com.zhuanbo.admin.api.dto.user.YinliUserDTO;
import com.zhuanbo.admin.api.dto.user.YinliUserInviteDTO;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.OSSPathEnum;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.core.entity.UserPartner;
import com.zhuanbo.core.notify.SmsSender;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserPartnerService;
import com.zhuanbo.service.service.IUserSecurityCenterService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


@Component
@Slf4j
public class YinliUserReceiver {

    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IUserPartnerService iUserPartnerService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IUserSecurityCenterService iUserSecurityCenterService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.zhuanbo-user-output.queue}", durable = "true"), exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.zhuanbo-user-output.routing-key}"))
    public void process(String msg, Channel channel, Message message) throws Exception {

        log.info("同步引力用户数据到[{}]", new String(message.getBody(), "UTF-8"));

        try {
            Map<String, Object> data = JSON.parseObject(new String(message.getBody(), "UTF-8"), new TypeReference<Map<String, Object>>() {});
//            YinliUserInviteDTO inviteDTO = BeanUtil.mapToBean(data, YinliUserInviteDTO.class, true);
//            yinliUserImport(inviteDTO);
            YinliUserDTO yinliUserDTO = BeanUtil.mapToBean(data, YinliUserDTO.class, true);
            update(yinliUserDTO);

        } catch (Exception e) {
            log.error("MQ处理同步用户数据失败:{}", e);

            iMqMessageService.tryOrStore(message);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        }

    }


//    private boolean yinliUserImport(YinliUserInviteDTO yinliUserInviteDTO) {
//        log.info("导入引力用户|请求参数" + JSON.toJSONString(yinliUserInviteDTO));
//
//        try {
//            YinliUserDTO yinliUserDTO1 = yinliUserInviteDTO.getPidUser();
//            if(null != yinliUserDTO1){
//                User pidUser = addOrUpdate(yinliUserDTO1);
//                YinliUserDTO yinliUserDTO = yinliUserInviteDTO.getUser();
//                User user = addOrUpdate(yinliUserDTO);
//                UserInvite userInviteTmp = iUserInviteService.getById(user.getId());
//                if(null != userInviteTmp){
//                    userInviteTmp.setPid(pidUser.getId());
//                    userInviteTmp.setInviteMonth(yinliUserInviteDTO.getInviteMonth());
//                    iUserInviteService.updateById(userInviteTmp);
//                }else{
//                    UserInvite userInvite = new UserInvite();
//                    userInvite.setId(user.getId());
//                    userInvite.setPid(pidUser.getId());
//                    userInvite.setInviteMonth(yinliUserInviteDTO.getInviteMonth());
//                    iUserInviteService.save(userInvite);
//                }
//            }else{
//                YinliUserDTO yinliUserDTO = yinliUserInviteDTO.getUser();
//                User user = addOrUpdate(yinliUserDTO);
//            }
//            return true;
//
//        } catch (Exception e) {
//            log.error("导入引力用户|发生异常，参数:{}", JSON.toJSONString(yinliUserInviteDTO),e);
//            return false;
//        }
//
//    }


    private User update(YinliUserDTO yinliUserDTO) {
        User user = iUserService.getOne(new QueryWrapper<User>().eq("mobile", yinliUserDTO.getMobile()));
        LocalDateTime now = LocalDateTime.now();
        if (null != user) {
//            BeanUtil.copyProperties(yinliUserDTO, user);
//            user.setUserName(yinliUserDTO.getMobile());
//            user.setNickname(yinliUserDTO.getName());
            user.setUpdateTime(now);
//            user.setPtLevel(getLevel(yinliUserDTO.getPtLevel()));
//            user.setStatus(yinliUserDTO.getStatus());
            iUserService.updateById(user);
            if(StringUtils.isNotBlank(yinliUserDTO.getAuthNo())){
                iUserPartnerService.update(new UserPartner(),new UpdateWrapper<UserPartner>().set("auth_no",yinliUserDTO.getAuthNo()).eq("id",user.getId()));
            }
        }
//        else {
//            user = new User();
//            BeanUtil.copyProperties(yinliUserDTO, user);
//            user.setUserName(yinliUserDTO.getMobile());
//            user.setNickname(yinliUserDTO.getName());
//            user.setGender(Integer.parseInt(ConstantsEnum.USER_GENDER_0.value().toString()));
////            user.setStatus(Integer.parseInt(ConstantsEnum.USER_STATUS_1.value().toString()));
//            user.setRegDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//            user.setRegTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
//            user.setAddTime(now);
//            user.setUpdateTime(now);
//
//            Object areaCode = RedisUtil.hget(ConstantsEnum.REDIS_KEY_AREA_CODE.value().toString(), yinliUserDTO.getMobile());
//            user.setAreaCode(areaCode == null ? "86" : areaCode.toString());
//            user.setPtLevel(getLevel(yinliUserDTO.getPtLevel()));
//            user.setStatus(yinliUserDTO.getStatus());
//            user.setInviteCode(iUserService.generateInviteCode());
//
//            iUserService.save(user);
//
//        }
//        // 副表
//        iUserPartnerService.simpleGenerate(user);
//        // 收益表
//        iUserIncomeService.makeUserIncome(user.getId());
//        // 用户安全中心
//        iUserSecurityCenterService.doUserSecurityCenter(user);
//        // 同步支付系统
//        iRabbitMQSenderService.send(RabbitMQSenderImpl.PAY_ADD, user);
//        // live
//        iRabbitMQSenderService.send(RabbitMQSenderImpl.LIVE_USER_ADD, user);
//        // 同步分润系统
//        iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
        return user;
    }

    private Integer getLevel(Integer yinliLevel){
        Integer result;
        switch (yinliLevel){
            case 0: case 1:
                result = 0;
                break;
            case 2:
                result = 1;
                break;
            case 3:
                result = 2;
                break;
            case 4:
                result = 3;
                break;
            case 5:
                result = 4;
                break;
            case 6:
                result = 5;
                break;
            case 7:
                result = 6;
                break;
            default:
                result = 0;
                break;
        }
        return result;
    }

}
