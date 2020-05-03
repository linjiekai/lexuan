package com.zhuanbo.shop.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.entity.*;
import com.zhuanbo.service.service.*;
import com.zhuanbo.service.vo.DynamicCommentVO;
import com.zhuanbo.service.vo.DynamicVO;
import com.zhuanbo.shop.api.dto.req.BaseParamsDTO;
import com.zhuanbo.shop.api.dto.req.CommentDTO;
import com.zhuanbo.shop.api.dto.resp.DynamicCommentDTO;
import com.zhuanbo.shop.api.dto.resp.DynamicDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shop/mobile/dynamic")
@Slf4j
public class MobileDynamicController {
    // 视频的第一帧:xxx.mp4?x-oss-process=video/snapshot,t_1,f_png,其中t_1中的1是指截图时间,单位:ms
    @Autowired
    private IDynamicCommentService iDynamicCommentService;
    @Autowired
    private IDynamicService iDynamicService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private IDynamicLikeService iDynamicLikeService;
    @Autowired
    private AuthConfig authConfig;


    /**
     * 获取动态列表
     * @param baseParamsDTO
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody BaseParamsDTO baseParamsDTO, HttpServletRequest request){
        if(baseParamsDTO ==null){
            baseParamsDTO = new BaseParamsDTO();
        }

        Page<DynamicVO> pageCond = new Page<>(baseParamsDTO.getPage(), baseParamsDTO.getLimit());
        IPage<DynamicVO> adIPage = iDynamicService.list(pageCond, getUserIdByRequest(request));
        List<DynamicDTO> dynamicDTOList = new ArrayList<>();
        for (DynamicVO dynamicVO:adIPage.getRecords()) {
            //截取视频第一帧图片
            DynamicDTO dynamicDTO = new DynamicDTO();
            BeanUtils.copyProperties(dynamicVO,dynamicDTO);
            dynamicDTO.setLikeNumber(countToK(dynamicVO.getLikeNumber()));
            dynamicDTO.setVideoImage(dynamicVO.getVideoImage()+"?x-oss-process=video/snapshot,t_1");
            dynamicDTO.setCommentCount(countDynamic(dynamicVO.getId()));
            dynamicDTO.setShareUrl(authConfig.getDynamicShareUrl()+dynamicDTO.getId());
            if(dynamicVO.getCoverImages() != null && dynamicVO.getCoverImages().length >0){
                dynamicDTO.setGoodsUrl(dynamicVO.getCoverImages()[0]+authConfig.getGoodIconStyle());
            }
            dynamicDTO.setDynamicCreateTime(iDynamicService.toHowLongTime(dynamicVO.getAddTime()));
            if (StringUtils.isNotBlank(dynamicVO.getVideoTranscodeUrl())) {
                dynamicDTO.setVideoUrl(dynamicVO.getVideoTranscodeUrl());
            }
            dynamicDTOList.add(dynamicDTO);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        data.put("items", dynamicDTOList);
        return ResponseUtil.ok(data);
    }


    /**
     * 获取动态详情
     * @param baseParamsDTO
     * @return
     */
    @PostMapping("/detail")
    public Object detail(@RequestBody BaseParamsDTO baseParamsDTO, HttpServletRequest request){

        Dynamic dynamic = iDynamicService.getById(baseParamsDTO.getId());
        if(dynamic == null){
            return ResponseUtil.result(70001);
        }
        Goods goods = null;
        if (dynamic.getGoodsId() != null) {
            goods = iGoodsService.getById(dynamic.getGoodsId());
        }
        User user = iUserService.getById(dynamic.getUserId());
        DynamicDTO dynamicDTO = new DynamicDTO();
        BeanUtils.copyProperties(dynamic,dynamicDTO);
        dynamicDTO.setLikeNumber(countToK(dynamic.getLikeNumber()));
        dynamicDTO.setHeadImgUrl(user.getHeadImgUrl());
        //截取视频第一帧图片
        dynamicDTO.setVideoImage(dynamic.getVideoUrl() + "?x-oss-process=video/snapshot,t_1");
        dynamicDTO.setNickname(user.getNickname());
        if (goods!= null){
            dynamicDTO.setGoodsId(goods.getId().longValue());
            dynamicDTO.setGoodsName(goods.getName());
            dynamicDTO.setGoodsSideName(goods.getSideName());
            if(goods.getCoverImages() != null && goods.getCoverImages().length >0){
                dynamicDTO.setGoodsUrl(goods.getCoverImages()[0]+authConfig.getGoodIconStyle());
            }
            dynamicDTO.setGoodsPrice(goods.getPrice());
        }
        dynamicDTO.setCommentCount(countDynamic(dynamic.getId()));
        dynamicDTO.setLiked(iDynamicLikeService.count(new QueryWrapper<DynamicLike>().eq("dynamic_id", dynamic.getId()).eq("user_id", getUserIdByRequest(request))));
        dynamicDTO.setShareUrl(authConfig.getDynamicShareUrl()+dynamic.getId());
        dynamicDTO.setDynamicCreateTime(iDynamicService.toHowLongTime(dynamic.getAddTime()));
        if (StringUtils.isNotBlank(dynamic.getVideoTranscodeUrl())) {
            dynamicDTO.setVideoUrl(dynamic.getVideoTranscodeUrl());
        }
        return ResponseUtil.ok(dynamicDTO);
    }

    /**
     * 获取评论列表
     * @param baseParamsDTO
     * @return
     */
    @PostMapping("/comment/list")
    public Object commentList(@RequestBody BaseParamsDTO baseParamsDTO, HttpServletRequest request){
        //获取登录用户
        Integer userId = getUserIdByRequest(request);
        //创建分页对象
        Page<DynamicCommentVO> pageCond = new Page<>(baseParamsDTO.getPage(), baseParamsDTO.getLimit());
        //获取查询结果
        IPage<DynamicCommentVO> adIPage = iDynamicCommentService.list(pageCond,baseParamsDTO.getDynamicId(), null);
        //获取评论数据
        List<DynamicCommentVO> dynamicVOList = adIPage.getRecords();
        //遍历评论数据
        for (DynamicCommentVO dynamicCommentVO:dynamicVOList) {
            //如果用不为空
            if(userId != null){
                //如果登录用户id等于评论id
                if(dynamicCommentVO.getFromUid().equals(userId)){
                    //设置是否自己评论为true
                    dynamicCommentVO.setIsOwn(true);
                }else {
                    //设置是否自己评论为false
                    dynamicCommentVO.setIsOwn(false);
                }
            } else {
                //为空就不是自己评论的
                dynamicCommentVO.setIsOwn(false);
            }
            //计算发言时间
            dynamicCommentVO.setDynamicTime(iDynamicService.toHowLongTime(dynamicCommentVO.getAddTime()));
            //如果pid不为0并且tousername为空
            if (dynamicCommentVO.getPid()!=0&&dynamicCommentVO.getToUserName()==null){
                //把tousername设置为空字符串
                dynamicCommentVO.setToUserName("");
            }
        }
        //创建返回结果map
        Map<String, Object> data = new HashMap<>();
        //获取总数
        data.put("total", adIPage.getTotal());
        //添加评论数据
        data.put("items", dynamicVOList);
        //把结果返回
        return ResponseUtil.ok(data);
    }
    /**
     * 评论添加
     * @param userId
     * @param commentDTO
     * @return
     */
    @PostMapping("/comment/create")
    public Object commentCreate(@LoginUser Long userId, @RequestBody CommentDTO commentDTO){

        if (commentDTO.getContent().length() > 100) {
            return ResponseUtil.result(60000);
        }
        User user = iUserService.getById(userId);
        if (user == null) {
            return ResponseUtil.badResult();
        }
        if (user.getShield() != null && user.getShield().equals(1)) {
            return ResponseUtil.result(60001);
        }

        //获取需要评论的对象
        DynamicComment dynamicCommentPid = null;
        if (commentDTO.getCommentId() != null && commentDTO.getCommentId().longValue() > 0) {
            dynamicCommentPid = iDynamicCommentService.getById(commentDTO.getCommentId());
            if(dynamicCommentPid == null){
                return ResponseUtil.result(60002);
            }
            if(dynamicCommentPid.getFromUid() != null && dynamicCommentPid.getFromUid().equals(userId)){
                return ResponseUtil.result(60003);
            }
        }
        DynamicComment dynamicComment = new DynamicComment();
        LocalDateTime now = LocalDateTime.now();
        dynamicComment.setFromUid(userId);
        dynamicComment.setToUid(dynamicCommentPid == null ? 0 : dynamicCommentPid.getFromUid());
        dynamicComment.setContent(commentDTO.getContent());
        dynamicComment.setDynamicId(commentDTO.getDynamicId());
        dynamicComment.setAddTime(now);
        dynamicComment.setUpdateTime(now);
        dynamicComment.setDeleted(0);
        dynamicComment.setChecked(0);
        dynamicComment.setPid(commentDTO.getCommentId() == null ? 0L : commentDTO.getCommentId());
        iDynamicCommentService.save(dynamicComment);

        Page<DynamicCommentVO> pageCond = new Page<>(1, 1);
        IPage<DynamicCommentVO> adIPage = iDynamicCommentService.list(pageCond, commentDTO.getDynamicId().intValue(), dynamicComment.getId().intValue());
        List<DynamicCommentVO> dynamicVOList = adIPage.getRecords();

        DynamicCommentVO dynamicCommentVO = dynamicVOList.get(0);
        dynamicCommentVO.setIsOwn(true);
        //计算发言时间
        dynamicCommentVO.setDynamicTime(iDynamicService.toHowLongTime(dynamicCommentVO.getAddTime()));
        int count = iDynamicCommentService.count(new QueryWrapper<DynamicComment>().eq("dynamic_id", commentDTO.getDynamicId())
                .eq("deleted", 0));
        return ResponseUtil.ok(MapUtil.of("comment", dynamicCommentVO, "total", countToK(count)));
    }

    /**
     * 评论详情
     * @param userId
     * @param dynamicComment
     * @return
     */
    @PostMapping("/comment/detail")
    public Object commentDetail(@LoginUser Long userId, @RequestBody DynamicComment dynamicComment){

        if(dynamicComment.getFromUid() != null && dynamicComment.getFromUid().equals(userId)){
            return ResponseUtil.result(60003);
        }
        dynamicComment = iDynamicCommentService.getById(dynamicComment.getId());
        if (dynamicComment == null) {
            log.error("动态不存在,id:{}", dynamicComment.getId());
            return ResponseUtil.badResult();
        }
        User fromUser = iUserService.getById(dynamicComment.getFromUid());
        if (fromUser == null) {
            log.error("动态的评论者不存在,id:{}", dynamicComment.getFromUid());
            return ResponseUtil.result(10007);
        }

        User toUser = null;
        if (dynamicComment.getToUid() != null && (!dynamicComment.getToUid().equals(0L))) {
            toUser = iUserService.getById(dynamicComment.getToUid());
            if (toUser == null) {
                log.error("动态的被评论者不存在,id:{}", dynamicComment.getToUid());
                return ResponseUtil.result(10007);
            }
        }
        DynamicCommentDTO dynamicCommentDTO = new DynamicCommentDTO();
        BeanUtils.copyProperties(dynamicComment, dynamicCommentDTO);
        dynamicCommentDTO.setFromUserName(fromUser.getUserName());
        dynamicCommentDTO.setFromUserHeader(fromUser.getHeadImgUrl());
        if (toUser != null) {
            dynamicCommentDTO.setToUserName(toUser.getUserName());
        }
        return dynamicCommentDTO;
    }

    /**likeNumber
     * 评论删除
     * @param userId
     * @param dynamicComment
     * @return
     */
    @PostMapping("/comment/delete")
    public Object commentDelete(@LoginUser Long userId, @RequestBody DynamicComment dynamicComment){

        dynamicComment = iDynamicCommentService.getById(dynamicComment.getId());
        if (dynamicComment == null) {
            log.error("动态不存在,id:{}", dynamicComment.getId());
            return ResponseUtil.result(60002);
        }

        if(!userId.equals(dynamicComment.getFromUid())){
            return ResponseUtil.result(60004);
        }
        if (!userId.equals(dynamicComment.getFromUid())) {
            return ResponseUtil.result(11112);
        }
        dynamicComment.setDeleted(1);
        iDynamicCommentService.updateById(dynamicComment);

        int count = iDynamicCommentService.count(new QueryWrapper<DynamicComment>().eq("dynamic_id", dynamicComment.getDynamicId())
                .eq("deleted", 0));
        return ResponseUtil.ok(MapUtil.of("total", countToK(count)));
    }

    /**
     * 点赞
     * @param userId
     * @param jsonObject
     * @return
     */
    @PostMapping("/like")
    public Object like(@LoginUser Long userId, @RequestBody JSONObject jsonObject){

        Long id = jsonObject.getLong("id");
        Integer like = jsonObject.getInteger("like");

        int count = iDynamicLikeService.count(new QueryWrapper<DynamicLike>().eq("dynamic_id", id).eq("user_id", userId));
        boolean flag = false;
        if (like.equals(1)) {// 点赞
            if (count == 0) {
                DynamicLike dynamicLike = new DynamicLike();
                dynamicLike.setDynamicId(id);
                dynamicLike.setUserId(userId);
                dynamicLike.setAddTime(LocalDateTime.now());
                flag = iDynamicLikeService.save(dynamicLike);
                
                if (flag) {
                	iDynamicService.updateLikeNumber(id, 1);
                }
            }
        } else {// 取消点赞
            if (count > 0) {
            	flag = iDynamicLikeService.remove(new QueryWrapper<DynamicLike>().eq("dynamic_id",id).eq("user_id", userId));
                if (flag) {
                	iDynamicService.updateLikeNumber(id, -1);
                }
            }
        }
        Dynamic dynamic = iDynamicService.getById(id);
        return ResponseUtil.ok(MapUtil.of("liked", like, "likeNumber", countToK(dynamic.getLikeNumber())));// 直接返回提示已点赞
    }

    private Integer getUserIdByRequest(HttpServletRequest request){
        Integer userId = null;
        if(request != null){
            String userToken = request.getHeader(Constants.LOGIN_TOKEN_KEY);
            if (StringUtils.isNotBlank(userToken)) {
                Object o = RedisUtil.get(userToken);
                if (o != null) {
                    userId = Integer.valueOf(o.toString());
                }
            }
        }
        return userId;
    }

    private String makeDynamicTime(LocalDateTime time){
        if(time != null){
            Duration duration = Duration.between(time,LocalDateTime.now());
            if(duration.toHours() == 0 && duration.toMinutes()>0){
                return duration.toMinutes()+"分钟前";
            }else if(duration.toHours() > 0 && duration.toHours()< 24 && duration.toMinutes()>0){
                return duration.toHours()+"小时"+ (duration.toMinutes() - duration.toHours() * 60)+"分钟前";
            }else if(duration.toHours() > 24 && duration.toHours() < 24*30){
                return ((int)duration.toHours()/24)+"天前";
            }else if(duration.toHours() > 24*30 && duration.toHours() < 24*30*12){
                return((int)duration.toHours()/ (24*30))+"月前";
            }else if(duration.toHours() > 24*30*12){
                return((int)duration.toHours()/ (24*30*12)) +"年前";
            }else{
                return "刚刚";
            }
        }
        return "未知时间";
    }
    private String countDynamic(Long dynamicId){
        int count = iDynamicCommentService.count(new QueryWrapper<DynamicComment>().eq("dynamic_id", dynamicId)
                .eq("deleted", 0));
        return countToK(count);
    }

    /**
     * 整数换成k
     * @param count
     * @return
     */
    private String countToK(Integer count) {
        if (count == null) {
            return "0";
        }
        if (count < 1000) {
            return count + "";
        } else if (count <= 999999) {
            BigDecimal divide = new BigDecimal(count).divide(new BigDecimal(1000), 1, RoundingMode.HALF_UP);
            return divide.doubleValue() + "k";
        } else {
            return "999k+";
        }
    }
}
