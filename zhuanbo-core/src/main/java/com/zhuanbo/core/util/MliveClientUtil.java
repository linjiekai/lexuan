package com.zhuanbo.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.dto.InvestorsPriceDTO;
import com.zhuanbo.core.exception.ShopException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MliveClientUtil {

    @Resource
    private OkHttpUtil okHttpUtil;

	public static InvestorsPriceDTO investors(String mliveAdminUrl, String params) {
        mliveAdminUrl = mliveAdminUrl + "/internal/investors/list";
        String result = HttpUtil.sendGet(mliveAdminUrl, params);
        if (StringUtils.isBlank(result)) {
            log.error("服务商投资价格列表result获取结果为空");
            throw new ShopException(30018, "服务商投资价格列表获取结果为空");
        }
        JSONObject resultJson;
        try {
        	resultJson = JSON.parseObject(result);
        } catch(Exception e) {
            log.error("服务商投资价格列表获取失败,内容解析失败");
            throw new ShopException(30019, "服务商投资价格列表获取失败,内容解析失败");
        }
        if (!ReqResEnum.C_10000.String().equalsIgnoreCase(resultJson.getString(ReqResEnum.CODE.String()))) {
            log.error("服务商投资价格列表获取失败:{}", resultJson.getString(ReqResEnum.MSG.String()));
            throw new ShopException(30020, "服务商投资价格列表获取失败:{}" + resultJson.getString(ReqResEnum.MSG.String()));
        }
        JSONObject data = resultJson.getJSONObject(ReqResEnum.DATA.String());
        JSONArray items = data.getJSONArray("items");
        
        return JSON.parseObject(items.get(0).toString(), InvestorsPriceDTO.class);
	}

    public List<Integer> checkUser(String mliveAdminUrl, Map<String, Object> params, String adminToken) {
        mliveAdminUrl = mliveAdminUrl + "/agent/user/check/register";
        Map<String, String> headMap = new HashMap<>();
        headMap.put("Admin-Token", adminToken);
        headMap.put("Connection", "keep-alive");
        String result = okHttpUtil.doGet(mliveAdminUrl, params, headMap);
        if (StringUtils.isBlank(result)) {
            log.error("服务商检测注册会员信息result获取结果为空");
            throw new ShopException(30018, "服务商检测注册会员信息结果为空");
        }
        JSONObject resultJson;
        try {
            resultJson = JSON.parseObject(result);
        } catch(Exception e) {
            log.error("服务商检测注册会员信息失败,内容解析失败");
            throw new ShopException(30019, "内容解析失败");
        }
        if (!ReqResEnum.C_10000.String().equalsIgnoreCase(resultJson.getString(ReqResEnum.CODE.String()))) {
            log.error("服务商检测注册会员信息获取失败:{}", resultJson.getString(ReqResEnum.MSG.String()));
            throw new ShopException(30020, resultJson.getString(ReqResEnum.MSG.String()));
        }
        JSONObject data = resultJson.getJSONObject(ReqResEnum.DATA.String());
        JSONArray items = data.getJSONArray("level");

        return items.toJavaObject(List.class);
    }
	
	public static void main(String[] args) {
//		InvestorsPriceDTO TO = investors("http://test-mlive-admin.zhuanbo.gdxfhl.com", "goodsType=3");
//		System.out.println(TO.getPlain());

//        List<Integer> integers = checkUser("http://test-mlive.zhuanbo.gdxfhl.com", "mobile=188133638883&areaCode=86", "ld7l1tw0zzh640fi1bd3q7dukb2ql09q");
//        System.out.println(integers);
	}
}
