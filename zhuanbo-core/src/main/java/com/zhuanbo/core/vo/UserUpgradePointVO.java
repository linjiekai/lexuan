package com.zhuanbo.core.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Administrator
 * @title: UserUpgradePointVO
 * @description: TODO
 * @date 2020/4/24 10:20
 */
@Data
public class UserUpgradePointVO implements Serializable {

    private Integer payType;

    private Map<Integer, Integer> levelPointInfo;
}
