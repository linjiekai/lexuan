package com.zhuanbo.admin.api.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.dto.user.YinliUserDTO;
import com.zhuanbo.admin.api.dto.user.YinliUserInviteDTO;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.VideoTransCodeEnum;
import com.zhuanbo.core.entity.Dynamic;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserInvite;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IDynamicService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserPartnerService;
import com.zhuanbo.service.service.IUserSecurityCenterService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service.IVideoTransCodeService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/init")
@Slf4j
public class InitController {

    @Autowired
    private IDynamicService iDynamicService;
    @Autowired
    private IVideoTransCodeService iVideoTransCodeService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private IUserPartnerService iUserPartnerService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IUserSecurityCenterService iUserSecurityCenterService;

    /**
     * 视频转码
     * @param key
     * @param mode
     * @return
     */
    @GetMapping("/transCode")
    public Object transCode(String key, String mode, Integer page) {
        if (!"ojsfoi9wFsoojF".equals(key)) {
            return 0;
        }

        int size = 0;
        if (VideoTransCodeEnum.DYNAMIC.value().equalsIgnoreCase(mode)) {

            IPage<Dynamic> pp = iDynamicService.page(new Page<>(page, 100),
                    new QueryWrapper<Dynamic>().eq("deleted", ConstantsEnum.DELETED_0.integerValue())
                            .eq("video_transcode_url", "")
                            .orderByDesc("id"));
            size = pp.getRecords().size();
            for (Dynamic dynamic : pp.getRecords()) {
                iVideoTransCodeService.sendTrans(dynamic);
            }
        } else if (VideoTransCodeEnum.GOODS.value().equalsIgnoreCase(mode)) {

            IPage<Goods> pp = iGoodsService.page(new Page<>(page, 100),
                    new QueryWrapper<Goods>().eq("deleted", ConstantsEnum.DELETED_0.integerValue())
                            .eq("video_transcode_url", "")
                            .orderByDesc("id"));
            size = pp.getRecords().size();
            for (Goods goods :  pp.getRecords()) {
                if (StringUtils.isNotBlank(goods.getVideoUrl())) {
                    iVideoTransCodeService.sendTrans(goods);
                }
            }
        }
        return size;
    }


    private User addOrUpdate(YinliUserDTO yinliUserDTO) {
        log.info("添加引力用户|请求参数" + JSON.toJSONString(yinliUserDTO));

        try {
            User user = addUser(yinliUserDTO);
            // 副表
            iUserPartnerService.simpleGenerate(user);
            // 收益表
            iUserIncomeService.makeUserIncome(user.getId());
            // 用户安全中心
            iUserSecurityCenterService.doUserSecurityCenter(user);
            // 同步支付系统
            iRabbitMQSenderService.send(RabbitMQSenderImpl.PAY_ADD, user);
            // live
            iRabbitMQSenderService.send(RabbitMQSenderImpl.LIVE_USER_ADD, user);
            // 同步分润系统
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);

            return user;
        } catch (Exception e) {
            log.error("添加引力用户|发生异常，参数:{}", JSON.toJSONString(yinliUserDTO),e);
            return null;
        }

    }

    private User addUser(YinliUserDTO yinliUserDTO) {
        User user = iUserService.getOne(new QueryWrapper<User>().eq("mobile", yinliUserDTO.getMobile()).eq("status", 1));
        LocalDateTime now = LocalDateTime.now();
        if (null != user) {
            BeanUtil.copyProperties(yinliUserDTO, user);
            user.setUserName(yinliUserDTO.getMobile());
            user.setNickname(yinliUserDTO.getName());
            user.setUpdateTime(now);
            iUserService.updateById(user);
        } else {
            user = new User();
            BeanUtil.copyProperties(yinliUserDTO, user);
            user.setUserName(yinliUserDTO.getMobile());
            user.setNickname(yinliUserDTO.getName());
            user.setGender(Integer.parseInt(ConstantsEnum.USER_GENDER_0.value().toString()));
            user.setStatus(Integer.parseInt(ConstantsEnum.USER_STATUS_1.value().toString()));
            user.setRegDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            user.setRegTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            user.setAddTime(now);
            user.setUpdateTime(now);

            Object areaCode = RedisUtil.hget(ConstantsEnum.REDIS_KEY_AREA_CODE.value().toString(), yinliUserDTO.getMobile());
            user.setAreaCode(areaCode == null ? "86" : areaCode.toString());
            user.setPtLevel(ConstantsEnum.USER_PT_LEVEL_0.integerValue());

            iUserService.save(user);
        }
        return user;
    }

    /**
     *  引力用户导入
     * @param yinliUserInviteDTO
     * @return
     */
    @RequestMapping("/yinli/user/import")
    @ResponseBody
    public Object yinliUserImport(@RequestBody YinliUserInviteDTO yinliUserInviteDTO) {
        log.info("导入引力用户|请求参数" + JSON.toJSONString(yinliUserInviteDTO));

        try {
            YinliUserDTO yinliUserDTO1 = yinliUserInviteDTO.getPidUser();
            if(null != yinliUserDTO1){
                User pidUser = addOrUpdate(yinliUserDTO1);
                YinliUserDTO yinliUserDTO = yinliUserInviteDTO.getUser();
                User user = addOrUpdate(yinliUserDTO);
                UserInvite userInviteTmp = iUserInviteService.getById(user.getId());
                if(null != userInviteTmp){
                    userInviteTmp.setPid(pidUser.getId());
                    userInviteTmp.setInviteMonth(yinliUserInviteDTO.getInviteMonth());
                    iUserInviteService.updateById(userInviteTmp);
                }else{
                    UserInvite userInvite = new UserInvite();
                    userInvite.setId(user.getId());
                    userInvite.setPid(pidUser.getId());
                    userInvite.setInviteMonth(yinliUserInviteDTO.getInviteMonth());
                    iUserInviteService.save(userInvite);
                }
            }else{
                YinliUserDTO yinliUserDTO = yinliUserInviteDTO.getUser();
                User user = addOrUpdate(yinliUserDTO);
            }
            return ResponseUtil.ok();

        } catch (Exception e) {
            log.error("导入引力用户|发生异常，参数:{}", JSON.toJSONString(yinliUserInviteDTO),e);
            return ResponseUtil.fail("99999","导入引力用户|发生异常");
        }

    }


    @GetMapping("/user/output")
    public Object userOutput(@RequestParam Integer type) throws Exception {
        List<User> userList = iUserService.xxx();//.list(new QueryWrapper<User>());

        try {
            if(null != userList && CollectionUtils.isNotEmpty(userList)){
                userOutputBat(userList,type);
            }
        } catch (Exception e) {
            log.error("同步用户数据到其他失败:{}", e);
            return ResponseUtil.fail();
        }

        return ResponseUtil.ok();
    }

    @GetMapping("/user/output/batch")
    public Object userOutputBatch(@RequestParam String ids, @RequestParam Integer type) throws Exception {

        String[] idArr = ids.split(";");
        List<String> idList = Arrays.asList(idArr);
        List<User> userList = (List<User>)iUserService.listByIds(idList);

        try {
            if(null != userList && CollectionUtils.isNotEmpty(userList)){
                userOutputBat(userList,type);
            }
        } catch (Exception e) {
            log.error("同步用户数据到其他失败:{}", e);
            return ResponseUtil.fail();
        }

        return ResponseUtil.ok();
    }

    private void userOutputBat(List<User> userList, Integer type) throws Exception {

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case 1:// 支付系统
                        for (User user : userList) {
                            log.info("同步用户数据到支付:[{}]", user.getId());
                            iRabbitMQSenderService.send(RabbitMQSenderImpl.PAY_ADD, user);
                        }
                        break;
                    case 2:// 支付系统
                        for (User user : userList) {
                            log.info("同步用户数据到live:[{}]", user.getId());
                            iRabbitMQSenderService.send(RabbitMQSenderImpl.LIVE_USER_ADD, user);
                        }
                        break;
                    case 3: // 分润系统
                        for (User user : userList) {
                            log.info("同步用户数据到分润:[{}]", user.getId());
                            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);
                        }
                        break;
                    case 32: // 分润系统
                        dd(userList);
                        break;
                    case 33: // 分润系统
                        userToPhp();
                        break;
                }
            }
        });
        executorService.shutdown();
    }

    private void dd(List<User> userList) {
        List<UserInvite> list = iUserInviteService.xxx();//.list(new QueryWrapper<UserInvite>());
        Map<Long, Long> inviteMap = list.stream().collect(Collectors.toMap(UserInvite::getId, UserInvite::getPid));
        LinkedList<Long> linkedList = new LinkedList<>();
        Long pid;
        int index = 0;
        for (User user : userList) {
            Long userId = user.getId();
            if(!linkedList.contains(userId)){
                linkedList.add(user.getId());
            }

            index = linkedList.indexOf(userId);
            if (inviteMap.containsKey(user.getId())) {
                pid = inviteMap.get(user.getId());
                if(linkedList.contains(pid)){
                    int pindex = linkedList.indexOf(pid);
                    if(pindex>index){
                        linkedList.remove(pid);
                        linkedList.add(index - 1, pid);
                    }
                }else{
                    linkedList.add(index - 1, pid);
                }
            }
        }

        log.info("=======ok============total:{}",linkedList.size());
        User u;
        for (Long id : linkedList) {
            u = new User();
            u.setId(id);
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, u);
        }
    }

    private void userToPhp() {
        List<User> userList = iUserService.list(new QueryWrapper<User>());
        Map<Long, User> userMap = new HashMap<Long, User>();
        for(User ui : userList){
            userMap.put(ui.getId(),ui);
        }

        Queue<Long> queue = new ArrayDeque<Long>();
        //总公司
        long rootId = 1;
        queue.offer(rootId);

        log.info("|同步用户数据到php|start");
        Long userId;
        User user;
        while (!queue.isEmpty()) {
            userId = queue.poll();
            user = userMap.get(userId);
            iRabbitMQSenderService.send(RabbitMQSenderImpl.USER_MODIFY_PROFIT, user);

            //子一级
            List<UserInvite> childrens = iUserInviteService.list(new QueryWrapper<UserInvite>().eq("pid",userId));
            if(CollectionUtil.isNotEmpty(childrens)){
                for(UserInvite ui : childrens){
                    queue.offer(ui.getId());
                }
            }
        }
        log.info("|同步用户数据到php|end");

    }
}
