package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.AppVersion;

/**
 * <p>
 * APP版本管理表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IAppVersionService extends IService<AppVersion> {
    /**
     * 校验app是否有效
     */
    void check();

    /**
     * 判断两个版本大小(根据点[.]拆分成相同的长度，不够的补0，然后根据数组一一对比)
     * @param oldVersion 旧版
     * @param newVersion 新版
     * @return 1:新版的大，-1:旧版的大，0：一样大
     */
    int isNew(String oldVersion, String newVersion);

    /**
     * 判断两个版本大小(根据点[.]拆分成相同的长度，不够的补0，然后根据数组一一对比)，专用于是否要邀请码
     * @param platform 平台
     * @param newVersion 新版
     * @return 1:新版的大(要邀请码)，-1:旧版的大（不要邀请码），0：一样大
     */
    int isNewForInviteCode(String platform, String newVersion);

    /**
     * 判断两个版本大小(根据点[.]拆分成相同的长度，不够的补0，然后根据数组一一对比)，专用于是否提现
     * @param platform 平台
     * @param newVersion 新版
     * @return 1:新版的大(要邀请码)，-1:旧版的大（不要邀请码），0：一样大
     */
    int isNewForTX(String platform, String newVersion);

    /**
     * 判断两个版本大小(根据点[.]拆分成相同的长度，不够的补0，然后根据数组一一对比)，专用于是否要邀请码(直接抛异常)
     * @param platform 平台
     * @param newVersion 新版
     * @param inviteCode 邀请码
     * @return 1:新版的大(要邀请码)，-1:旧版的大（不要邀请码），0：一样大
     */
    void isNewForInviteCodeThrowEx(String platform, String newVersion, String inviteCode);
}
