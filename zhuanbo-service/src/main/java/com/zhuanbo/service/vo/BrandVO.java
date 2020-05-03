package com.zhuanbo.service.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: Jiekai Lin
 * @Description(描述):  返回给前端VO，扩展字段都放在这里
 * @date: 2019/7/27 10:51
 */
@Data
public class BrandVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 明星封面
     */
    private String starCover;

    /**
     * 品牌名称
     */
    private String name;

   /**
     * 序号
     */
    private Integer indexs;

    /**
     * 品牌简介
     */
    private String content;

    /**
     * logo
     */
    private String logo;

    /**
     * 品牌详情封面
     */
    private String detailCover;

    /**
     * 状态：0：下线，1：上线
     */
    private Integer status;
    /**
     * 逻辑删除，0：未删除，1：删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */

    private LocalDateTime updateTime;

    /**
     * 操作人名称
     */
    private String operator;

    List<GoodsVo> goods;


}
