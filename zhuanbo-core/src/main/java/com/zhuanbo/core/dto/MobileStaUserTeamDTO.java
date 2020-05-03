package com.zhuanbo.core.dto;

import lombok.Data;

import java.util.List;

@Data
public class MobileStaUserTeamDTO extends MobileBaseParamsDTO {

    private List<Long> userIds;

    private Integer ptLevel;//4:M司令(合伙人)5:M司令(高级合伙人)6:M司令(总监)-1:普通

    private Integer Teamtype; //团队类型，0：普通邀请团队 ，1：合伙人团队

    private String teamName; //团队名称

}
