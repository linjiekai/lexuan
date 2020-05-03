package com.zhuanbo.service.utils;

import com.zhuanbo.core.entity.OperateLog;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.SpringContextUtil;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IOperateLogService;
import com.zhuanbo.service.service.IUserService;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

;

public class LogOperateUtil {

    private static Logger logger = LoggerFactory.getLogger(LogOperateUtil.class);

    private static IOperateLogService iOperateLogService;
    private static IAdminService iAdminService;
    private static IUserService iUserService;

    static {
        iOperateLogService = (IOperateLogService) SpringContextUtil.getBean("operateLogServiceImpl");
        iAdminService = (IAdminService) SpringContextUtil.getBean("adminServiceImpl");
        iUserService = (IUserService) SpringContextUtil.getBean("userServiceImpl");
    }

    /**
     * @param type      模块类型
     * @param action    动作
     * @param targetId  操作目标id
     * @param operateId 操作人
     * @param userType  0：管理后台用户，1：user用户
     */
    public static void log(String type, String action, String targetId, Long operateId, Integer userType) {
        try {
            LocalDateTime now = LocalDateTime.now();
            OperateLog operateLog = new OperateLog();
            operateLog.setOperateType(type);
            operateLog.setOperateAction(action);
            operateLog.setClientIp((String) MDC.get("CLIENT_IP"));
            if (userType.equals(0)) {
                operateLog.setOperator(iAdminService.getAdminName(operateId.intValue()));
                operateLog.setOperateId(operateId);
            } else {
                User user = iUserService.getById(operateId);
                if (user != null) {
                    operateLog.setOperator(user.getNickname());
                }
                operateLog.setOperateId(operateId);
            }
            operateLog.setTargetId(targetId);
            operateLog.setOperateDate(DateUtil.toyyyy_MM_dd(now));
            operateLog.setOperateTime(DateUtil.toHH_mm_ss(now));
            operateLog.setAddTime(now);
            iOperateLogService.save(operateLog);
        } catch (Exception e) {
            logger.error("==》记录操作日志失败:{},参数：{}", e, type + ":" + action + ":" + targetId + ":" + operateId + ":" + userType);
        }
    }
}
