package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.AppVersion;
import com.zhuanbo.service.mapper.AppVersionMapper;
import com.zhuanbo.service.service.IAppVersionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * APP版本管理表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements IAppVersionService {

    // 版本号
    private final String V12 = "1.2";
    private final String V10 = "1.0";

    @Override
    public void check() {

        List<AppVersion> list = list(new QueryWrapper<AppVersion>().eq("status", 0)
                .eq("deleted", ConstantsEnum.DELETED_0.integerValue()).orderByDesc("eff_time").orderByDesc("id"));
        if (list.isEmpty()) {
            return;
        }
        List<Long> stillWaitEffectiveIds = new ArrayList<>();// 一直保持待生效的app
        LocalDateTime now = LocalDateTime.now();
        Map<String, List<AppVersion>> collect = list.stream().collect(Collectors.groupingBy(x -> x.getPlatform() + x.getSysCnl()));// 按平台与系统分
        Iterator<Map.Entry<String, List<AppVersion>>> iterator = collect.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<AppVersion>> next = iterator.next();
            if (next.getValue() == null || next.getValue().size() == 0) {
                continue;
            }
            for (AppVersion appVersion : next.getValue()) {
                if (appVersion.getEffTime().isAfter(now)) {
                    stillWaitEffectiveIds.add(appVersion.getId());
                    continue;
                }
                stillWaitEffectiveIds.add(appVersion.getId());// 其他的全部失效
                update(new AppVersion(), new UpdateWrapper<AppVersion>()
                        .set("status", 2).eq("sys_cnl", appVersion.getSysCnl()).eq("platform", appVersion.getPlatform()).notIn("id", stillWaitEffectiveIds));
                appVersion.setStatus(1);
                updateById(appVersion);
                break;
            }
        }
    }

    @Override
    public int isNew(String oldVersion, String newVersion) {
        String[] splitOld = oldVersion.split("\\.");
        String[] splitNew = newVersion.split("\\.");
        if (splitOld.length > splitNew.length) {
            String z = "0";
            String[] newSplitNew = new String[splitOld.length];
            for (int i = 0; i < newSplitNew.length; i++) {
                if (i < splitNew.length) {
                    newSplitNew[i] = splitNew[i];
                } else {
                    newSplitNew[i] = z;
                }
            }
            splitNew = newSplitNew;
        } else if (splitNew.length > splitOld.length) {
            String z = "0";
            String[] newSplitOld = new String[splitNew.length];
            for (int i = 0; i < newSplitOld.length; i++) {
                if (i < splitOld.length) {
                    newSplitOld[i] = splitOld[i];
                } else {
                    newSplitOld[i] = z;
                }
            }
            splitOld = newSplitOld;
        }
        for (int i = 0; i < splitOld.length; i++) {
            if (Integer.parseInt(splitOld[i]) < Integer.parseInt(splitNew[i])) {
                return 1;
            } else if (Integer.parseInt(splitOld[i]) > Integer.parseInt(splitNew[i])){
                return -1;
            }
        }
        return 0;
    }

    @Override
    public int isNewForInviteCode(String platform, String newVersion) {
        if (StringUtils.isBlank(newVersion)) {
            return -1;
        }
        if (newVersion.contains("(")) {// IOS旧版问题
            newVersion = newVersion.substring(0, newVersion.indexOf("("));
        }
        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(platform)) {
            return isNew(V12, newVersion);
        } else if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(platform)){
            return isNew(V10, newVersion);
        } else {
            return isNew(V12, newVersion);// 微信没有platform，传过来1.3就是在邀请码了
        }
    }

    @Override
    public int isNewForTX(String platform, String newVersion) {
        if (StringUtils.isBlank(newVersion)) {
            return -1;
        }
        if (newVersion.contains("(")) {// IOS旧版问题
            newVersion = newVersion.substring(0, newVersion.indexOf("("));
        }
        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(platform)) {
            return isNew(V12, newVersion);
        } else if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(platform)){
            return isNew(V10, newVersion);
        } else {
            return isNew(V12, newVersion);// 微信没有platform，传过来1.3就是在邀请码了
        }
    }

    @Override
    public void isNewForInviteCodeThrowEx(String platform, String newVersion, String inviteCode) {
        // StringUtils.isBlank(inviteCode) && StringUtils.isBlank(newVersion) && isNewForInviteCode(platform, newVersion) == 1
        if (StringUtils.isBlank(inviteCode)) {
            throw new ShopException(10047);
        }
    }
}
