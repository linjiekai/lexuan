package com.zhuanbo.service.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.core.entity.ShowCategory;
import lombok.Data;

import java.util.List;

/**
 * @Description(描述): 扩展字段都放在这里
 * @auther: Jack Lin
 * @param :
 * @return :
 * @date: 2019/7/11 18:05
 */
@Data
public class ShowCategoryVO extends ShowCategory {

    private static final long serialVersionUID = 1L;
    /**
     * 父类目名称
     */
    @TableField(exist = false)
    private String pidName;

    /**
     * 一级分类关联的产品
     */
    @TableField(exist = false)
    private List<Goods> goods;
}
