package com.manzhushaka.system.application.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.application.service.SystemAuditAppService;
import com.manzhushaka.system.domain.SysLogininfor;
import com.manzhushaka.system.domain.SysOperLog;
import com.manzhushaka.system.domain.SysSlowSqlLog;
import com.manzhushaka.system.service.ISysLogininforService;
import com.manzhushaka.system.service.ISysOperLogService;
import com.manzhushaka.system.service.ISysSlowSqlLogService;

/**
 * 系统审计应用服务实现
 * <p>
 * 封装操作日志和登录日志的持久化逻辑，
 * 供 framework 模块通过 {@link SystemAuditAppService} 接口调用。
 * </p>
 * 
 * @author manzhushaka
 */
@Service
public class SystemAuditAppServiceImpl implements SystemAuditAppService
{
    private static final Logger log = LoggerFactory.getLogger(SystemAuditAppServiceImpl.class);

    @Autowired
    private ISysLogininforService logininforService;

    @Autowired
    private ISysOperLogService operLogService;

    @Autowired
    private ISysSlowSqlLogService slowSqlLogService;

    @Override
    public void recordLoginAudit(String username, String status, String message, String ip,
                                  String os, String browser, String address)
    {
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        logininfor.setIpaddr(ip);
        logininfor.setLoginLocation(address);
        logininfor.setBrowser(browser);
        logininfor.setOs(os);
        logininfor.setMsg(message);

        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
        {
            logininfor.setStatus(Constants.SUCCESS);
        }
        else if (Constants.LOGIN_FAIL.equals(status))
        {
            logininfor.setStatus(Constants.FAIL);
        }

        logininforService.insertLogininfor(logininfor);
    }

    @Override
    public void recordOperationAudit(String operIp, String operLocation, String operName, String deptName,
                                      String method, String requestMethod, String operUrl, String operParam,
                                      String jsonResult, Integer status, String errorMsg, Integer businessType,
                                      String title, Integer operatorType, Long costTime)
    {
        SysOperLog operLog = new SysOperLog();
        operLog.setOperIp(operIp);
        operLog.setOperLocation(operLocation);
        operLog.setOperName(operName);
        operLog.setDeptName(deptName);
        operLog.setMethod(method);
        operLog.setRequestMethod(requestMethod);
        operLog.setOperUrl(operUrl);
        operLog.setOperParam(operParam);
        operLog.setJsonResult(jsonResult);
        operLog.setStatus(status);
        operLog.setErrorMsg(errorMsg);
        operLog.setBusinessType(businessType);
        operLog.setTitle(title);
        operLog.setOperatorType(operatorType);
        operLog.setCostTime(costTime);

        // 远程查询操作地点
        operLog.setOperLocation(com.manzhushaka.common.utils.ip.AddressUtils.getRealAddressByIP(operIp));

        operLogService.insertOperlog(operLog);
    }

    @Override
    public void recordSlowSqlLog(String mapperId, String sqlText, String dataSourceName, Long costTime,
                                  String errorMsg, java.util.Date executeTime)
    {
        SysSlowSqlLog slowSqlLog = new SysSlowSqlLog();
        slowSqlLog.setMapperId(mapperId);
        slowSqlLog.setSqlText(sqlText);
        slowSqlLog.setDataSourceName(dataSourceName);
        slowSqlLog.setCostTime(costTime);
        slowSqlLog.setErrorMsg(errorMsg);
        slowSqlLog.setExecuteTime(executeTime);
        slowSqlLogService.insertSlowSqlLog(slowSqlLog);
    }
}
