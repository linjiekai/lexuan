package com.zhuanbo.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类（要配置logback-spring.xml文件:<logger name="xxx" addtivity="false"></logger>）
 */
public class LogUtil {
	// 分润日志
	public final static Logger SHARE_PROFIT = LoggerFactory.getLogger("shareProfit");
	// admin的日志
	public final static Logger ADMIN_LOG = LoggerFactory.getLogger("adminLog");
	// 冷响应日志
	public final static Logger COLD_FILE = LoggerFactory.getLogger("coldFile");
	// 定时器日志
	public final static Logger SCHEDULED = LoggerFactory.getLogger("scheduled");
}
