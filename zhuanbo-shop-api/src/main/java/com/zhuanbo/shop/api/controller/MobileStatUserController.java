package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.dto.AdminUserPartnerCountDTO;
import com.zhuanbo.core.dto.MobileStaUserTeamDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserPartner;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IStatUserSaleDayService;
import com.zhuanbo.service.service.IUserInviteService;
import com.zhuanbo.service.service.IUserPartnerService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.vo.StatUserTeamVO;
import com.zhuanbo.service.vo.UserLoginVO;
import com.zhuanbo.shop.api.dto.req.StatUserTeamDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shop/mobile/stat/user")
@Slf4j
public class MobileStatUserController {

    @Autowired
    private IUserInviteService iUserInviteService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IStatUserSaleDayService iStatUserSaleDayService;
    @Autowired
    private IUserPartnerService iUserPartnerService;
    /*@Autowired
    private IGraphService iGraphService;*/

    /**
     * (弃用)
     * @param userId
     * @param dto
     * @return
     * @throws Exception
     */
    @Deprecated
    @PostMapping("/team/sale")
    public Object teamSale(@LoginUser Long userId, @RequestBody StatUserTeamDTO dto) throws Exception {

        User user = iUserService.getById(userId);
        if (null == user) {
            return ResponseUtil.result(10007);
        }
        List<Long> userIds = iUserInviteService.getTeamFilterId(userId);
        userIds.add(userId);

        dto.setUserIds(userIds);

        Page<StatUserTeamVO> page = new Page<>(dto.getPage(), dto.getLimit());

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(BeanUtils.beanToMap(dto));

        log.info("page=" + dto.getPage() + "limit=" + dto.getLimit());
        IPage<StatUserTeamVO> iPage = iStatUserSaleDayService.statUserSale(page, params);
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());
        data.put("items", iPage.getRecords());
        log.info("iPage.getTotal()=" + iPage.getTotal());
        return ResponseUtil.ok(data);
    }

    /**
     * @param :[userId, statUserTeamDTO]
     * @return :java.lang.Object
     * @Description(描述): 我的团队 (弃用)
     * @auther: Jack Lin
     * @date: 2019/8/21 16:03
     */
    @Deprecated
    @PostMapping("/team")
    public Object team(@LoginUser Long userId, @RequestBody MobileStaUserTeamDTO statUserTeamDTO) throws Exception {
        statUserTeamDTO.setUserId(userId);
        statUserTeamDTO.setTeamtype(0);
        Map<String, Object> team = iUserService.team(statUserTeamDTO);
        Map<String, Object> teamCount = iUserService.teamCount(statUserTeamDTO);
        List<AdminUserPartnerCountDTO> items = (List<AdminUserPartnerCountDTO>)teamCount.get("items");
        int plainCount = 0;
        int plusCount = 0;
        int trainCount = 0;
        int servCount = 0;
        for(AdminUserPartnerCountDTO item : items){
           switch (item.getPtLevel()){
               case 0:plainCount=item.getCount();break;
               case 1:plusCount=item.getCount();break;
               case 2:trainCount=item.getCount();break;
               case 3:servCount=item.getCount();break;
               default: break;
           }
        }
        Map<String, Object> levelCount = new HashMap<>();
        //总人数+1：1是包括自己
        levelCount.put("totalCount", (Integer)teamCount.get("total")+1);
        levelCount.put("plainCount", plainCount);
        levelCount.put("plusCount", plusCount);
        levelCount.put("trainCount", trainCount);
        levelCount.put("servCount", servCount);

        Map<String, Object> data = new HashMap<>();
        data.put("total", team.get("total"));
        data.put("levelCount", levelCount);
        data.put("items", team.get("items"));
        return ResponseUtil.ok(data);
    }

    @PostMapping("/partner/team")
    public Object partnerTeam(@LoginUser Long userId, @RequestBody MobileStaUserTeamDTO statUserTeamDTO) {
        return ResponseUtil.ok();
        /*GUserVO gUserVO;

        if (statUserTeamDTO.getPtLevel().equals(0)){

            gUserVO = iGraphService.teamListStart(userId, statUserTeamDTO.getPage(), statUserTeamDTO.getLimit());
        } else {

            User user = iUserService.getById(userId);
            gUserVO = iGraphService.teamList(user, statUserTeamDTO.getPtLevel(), statUserTeamDTO.getPage(), statUserTeamDTO.getLimit());
        }
        return ResponseUtil.ok(gUserVO);*/
    }

    /**
     * 团队成员数量
     * @param userId
     * @return
     * @throws Exception
     */
    @PostMapping("/partner/team/count")
    public Object partnerTeamCount(@LoginUser Long userId) {
        //return ResponseUtil.ok(iGraphService.teamCountMap(iUserService.getById(userId)));
        return ResponseUtil.ok();
    }

    @PostMapping("/partner/team/updateTeamName")
    public Object updateTeamName(@LoginUser Long userId,@RequestBody MobileStaUserTeamDTO statUserTeamDTO, HttpServletRequest request) throws Exception {
        String teamName = statUserTeamDTO.getTeamName();
        teamName = teamName.trim();
        if(StringUtils.isEmpty(teamName)){
            teamName = "我的团队";
        }
        iUserPartnerService.update(new UserPartner(), new UpdateWrapper<UserPartner>().eq("id", userId).set("team_name", teamName));
        User u =  iUserService.getById(userId);
        UserLoginVO userLoginVO = iUserService.packageUser(u, false);
        userLoginVO.setUserToken(request.getHeader(Constants.LOGIN_TOKEN_KEY));
        return ResponseUtil.ok(userLoginVO);
    }


}
