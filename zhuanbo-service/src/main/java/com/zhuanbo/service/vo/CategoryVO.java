package com.zhuanbo.service.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.zhuanbo.core.entity.Category;
import lombok.Data;

/**
 * @Description(描述): 扩展字段都放在这里
 * @auther: Jack Lin
 * @param :
 * @return :
 * @date: 2019/7/11 17:28
 */
@Data
public class CategoryVO extends Category {
    /**
     * 父类目名称
     */
    @TableField(exist = false)
    private String pidName;
}
