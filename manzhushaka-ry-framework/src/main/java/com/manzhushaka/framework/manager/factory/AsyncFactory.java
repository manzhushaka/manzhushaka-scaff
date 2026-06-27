package com.manzhushaka.framework.manager.factory;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.utils.LogUtils;
import com.manzhushaka.common.utils.ServletUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.http.UserAgentUtils;
import com.manzhushaka.common.utils.ip.AddressUtils;
import com.manzhushaka.common.utils.ip.IpUtils;
import com.manzhushaka.common.utils.spring.SpringUtils;
import com.manzhushaka.framework.web.command.LoginAuditRecord;
import com.manzhushaka.framework.web.command.OperationAuditRecord;
import com.manzhushaka.system.application.service.SystemAuditAppService;

/**
 * 异步工厂（产生任务用）
 * 
 * @author manzhushaka
 */
public class AsyncFactory
{
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 记录登录信息
     * 
     * @param username 用户名
     * @param status 状态
     * @param message 消息
     * @param args 列表
     * @return 任务task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
            final Object... args)
    {
        final String userAgent = ServletUtils.getRequest().getHeader("User-Agent");
        final String ip = IpUtils.getIpAddr();
        return new TimerTask()
        {
            @Override
            public void run()
            {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = UserAgentUtils.getOperatingSystem(userAgent);
                // 获取客户端浏览器
                String browser = UserAgentUtils.getBrowser(userAgent);
                // 通过应用服务记录登录审计
                SpringUtils.getBean(SystemAuditAppService.class).recordLoginAudit(
                        username, status, message, ip, os, browser, address);
            }
        };
    }

    /**
     * 操作日志记录
     * 
     * @param operLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final OperationAuditRecord operLog)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                SpringUtils.getBean(SystemAuditAppService.class).recordOperationAudit(
                        operLog.operIp(), null, operLog.operName(), operLog.deptName(),
                        operLog.method(), operLog.requestMethod(), operLog.operUrl(),
                        operLog.operParam(), operLog.jsonResult(), operLog.status(),
                        operLog.errorMsg(), operLog.businessType(), operLog.title(),
                        operLog.operatorType(), operLog.costTime());
            }
        };
    }
}
