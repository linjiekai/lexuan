package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.admin.api.vo.UserIncomeDetailVO;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.dto.AdminUserIncomeDetailDTO;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.service.service.ICipherService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 * 收益管理
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Slf4j
@RestController
@RequestMapping("/admin/user/income/detail")
public class UserIncomeDetailController {

    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ICipherService cipherService;


    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId, @RequestBody AdminUserIncomeDetailDTO detailDTO) {

        IPage<UserIncomeDetails> iPage = new Page<>(detailDTO.getPage(), detailDTO.getLimit());
        QueryWrapper<UserIncomeDetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("add_time"); //变更时间
        Optional.ofNullable(detailDTO.getUserId()).ifPresent(s->queryWrapper.eq("user_id", s)); //用户id
        Optional.ofNullable(detailDTO.getFromUserId()).ifPresent(s->queryWrapper.eq("from_user_id", s));
        Optional.ofNullable(detailDTO.getWithDrawType()).ifPresent(s->queryWrapper.eq("with_draw_type", s));
        Optional.ofNullable(detailDTO.getIncomeType()).ifPresent(s->queryWrapper.eq("income_type", s));
        if (StringUtils.isNotBlank(detailDTO.getOrderNo())) {
            queryWrapper.eq("order_no", detailDTO.getOrderNo());
        }

        IPage<UserIncomeDetails> userIncomeDetailsIPage = iUserIncomeDetailsService.page(iPage, queryWrapper);
        List<UserIncomeDetails> detailList = userIncomeDetailsIPage.getRecords();
        Map<String, Object> backMap = new HashMap<>();
        backMap.put("total", userIncomeDetailsIPage.getTotal());

        // 银行信息完善
        List<String> bankCodes = detailList.stream().map(UserIncomeDetails::getBankCode).distinct().collect(toList());
        List<Dictionary> banks = dictionaryService.list(new QueryWrapper<Dictionary>()
                .eq("category", "bankCode").in("str_val", bankCodes));

        List<UserIncomeDetailVO> detailVOList = new ArrayList<>();
        detailList.forEach(detail -> {
            UserIncomeDetailVO userIncomeDetailVO = new UserIncomeDetailVO();
            BeanUtils.copyProperties(detail, userIncomeDetailVO);
            // 用户昵称处理
            User user = iUserService.getById(detail.getUserId());
            if (user != null) {
                userIncomeDetailVO.setNickname(user.getNickname());
                userIncomeDetailVO.setPtLevel(user.getPtLevel());
            }
            // 银行信息处理
            String bankCode = detail.getBankCode();
            if(banks != null && banks.size() > 0){
                banks.forEach(bank -> {
                    if(StringUtils.isNotBlank(bankCode) && bankCode.equals(bank.getStrVal())){
                        userIncomeDetailVO.setBankName(bank.getName());
                    }
                });
            }
            // 银行卡脱敏处理
            String bankCardNo = userIncomeDetailVO.getBankCardNo();
            if (StringUtils.isNotBlank(bankCardNo)) {
                try {
                    bankCardNo = cipherService.decryptAES(bankCardNo);
                } catch (Exception e) {
                    log.error("|收益明细列表|银行卡解密失败|银行卡号:{}|", bankCardNo);
                }
                bankCardNo = bankCardNo.substring(0, bankCardNo.length() - 8) + "****" + bankCardNo.substring(bankCardNo.length() - 4);
                userIncomeDetailVO.setBankCardNo(bankCardNo);
            }
            detailVOList.add(userIncomeDetailVO);
        });
        backMap.put("items", detailVOList);
        return ResponseUtil.ok(backMap);
    }
}
