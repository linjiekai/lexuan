package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * 行政区域表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_region")
public class Region implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 行政区域父ID，例如区县的pid指向市，市的pid指向省，省的pid则是0
     */
    @NotNull(message = "区域父ID不能为空")
    @Min(value = 0, message = "区域父ID最小为0")
    private Integer pid;

    /**
     * 行政区域名称
     */
    @NotBlank(message = "行政区域名称不能为空")
    private String name;

    /**
     * 行政区域类型，如如1则是省， 如果是2则是市，如果是3则是区县
     */
    @NotNull(message = "区域类型不能为空")
    @Min(value = 0, message = "区域类型最小为0")
    private Integer type;

    /**
     * 行政区域编码
     */
    @NotNull(message = "行政区域编号不能为空")
    private Integer code;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Region{" +
                "id=" + id +
                ", pid=" + pid +
                ", name=" + name +
                ", type=" + type +
                ", code=" + code +
                "}";
    }
}
