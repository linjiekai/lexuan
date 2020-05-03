package com.zhuanbo.shop.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.CommonQuestion;
import com.zhuanbo.service.service.ICommonQuestionService;
import com.zhuanbo.shop.api.dto.req.CommentQuestionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广告
 */
@RestController
@RequestMapping("/shop/mobile/common/question")
public class MobileCommonQuestionController {

    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private ICommonQuestionService iCommonQuestionService;

    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@RequestBody CommentQuestionDTO commentQuestionDTO) {

        IPage<CommonQuestion> iPage = new Page<>(commentQuestionDTO.getPage(), commentQuestionDTO.getLimit());
        IPage<CommonQuestion> commonQuestionIPage= iCommonQuestionService.page(iPage,
                new QueryWrapper<CommonQuestion>()
                        .eq("deleted", ConstantsEnum.DELETED_0.integerValue())
                        .eq("platform", commentQuestionDTO.getPlatform())
                        .eq("position", commentQuestionDTO.getPosition())
                        .orderByDesc("serial_number"));
        return ResponseUtil.ok(MapUtil.of("total", commonQuestionIPage.getTotal(), "items", commonQuestionIPage.getRecords()));
    }

}
