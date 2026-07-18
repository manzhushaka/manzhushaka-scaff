package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.application.query.LoginLogQuery;
import com.manzhushaka.system.application.query.MqMessageLogQuery;
import com.manzhushaka.system.application.query.OperLogQuery;
import com.manzhushaka.system.application.query.SlowSqlLogQuery;
import com.manzhushaka.system.application.result.system.LoginLogResult;
import com.manzhushaka.system.application.result.system.MqMessageLogDetailResult;
import com.manzhushaka.system.application.result.system.MqMessageLogResult;
import com.manzhushaka.system.application.result.system.OperLogResult;
import com.manzhushaka.system.application.result.system.SlowSqlLogResult;
import com.manzhushaka.system.application.service.SystemAuditAppService;
import com.manzhushaka.system.domain.SysLogininfor;
import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.domain.SysOperLog;
import com.manzhushaka.system.domain.SysSlowSqlLog;
import com.manzhushaka.system.service.ISysLogininforService;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import com.manzhushaka.system.service.ISysOperLogService;
import com.manzhushaka.system.service.ISysSlowSqlLogService;

/**
 * 系统审计应用服务实现
 * 封装操作日志和登录日志的持久化逻辑，
 * 供 framework 模块通过 {@link SystemAuditAppService} 接口调用。
 * 
 * @author manzhushaka
 */
@Service
public class SystemAuditAppServiceImpl implements SystemAuditAppService
{
    @Autowired
    private ISysLogininforService logininforService;

    @Autowired
    private ISysOperLogService operLogService;

    @Autowired
    private ISysSlowSqlLogService slowSqlLogService;

    @Autowired
    private ISysMqMessageLogService mqMessageLogService;

    @Override
    public List<OperLogResult> listOperationLogs(OperLogQuery query)
    {
        return operLogService.selectOperLogList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public OperLogResult getOperationLog(Long operId)
    {
        return toResult(operLogService.selectOperLogById(operId));
    }

    @Override
    public int deleteOperationLogs(Long[] operIds)
    {
        return operLogService.deleteOperLogByIds(operIds);
    }

    @Override
    public void cleanOperationLogs()
    {
        operLogService.cleanOperLog();
    }

    @Override
    public List<LoginLogResult> listLoginLogs(LoginLogQuery query)
    {
        return logininforService.selectLogininforList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public int deleteLoginLogs(Long[] infoIds)
    {
        return logininforService.deleteLogininforByIds(infoIds);
    }

    @Override
    public void cleanLoginLogs()
    {
        logininforService.cleanLogininfor();
    }

    @Override
    public List<SlowSqlLogResult> listSlowSqlLogs(SlowSqlLogQuery query)
    {
        return slowSqlLogService.selectSlowSqlLogList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public SlowSqlLogResult getSlowSqlLog(Long slowSqlId)
    {
        return toResult(slowSqlLogService.selectSlowSqlLogById(slowSqlId));
    }

    @Override
    public int deleteSlowSqlLogs(Long[] slowSqlIds)
    {
        return slowSqlLogService.deleteSlowSqlLogByIds(slowSqlIds);
    }

    @Override
    public void cleanSlowSqlLogs()
    {
        slowSqlLogService.cleanSlowSqlLog();
    }

    @Override
    public List<MqMessageLogResult> listMqMessageLogs(MqMessageLogQuery query)
    {
        return mqMessageLogService.selectMessageLogList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public MqMessageLogResult getMqMessageLog(Long messageLogId)
    {
        return toResult(mqMessageLogService.selectMessageLogById(messageLogId));
    }

    @Override
    public List<MqMessageLogDetailResult> listMqMessageLogDetails(Long messageLogId)
    {
        return mqMessageLogService.selectDetailListByMessageLogId(messageLogId).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public int deleteMqMessageLogs(Long[] messageLogIds)
    {
        return mqMessageLogService.deleteMessageLogByIds(messageLogIds);
    }

    @Override
    public void cleanMqMessageLogs()
    {
        mqMessageLogService.cleanMessageLog();
    }

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

    private SysOperLog toEntity(OperLogQuery query)
    {
        SysOperLog log = new SysOperLog();
        if (query == null)
        {
            return log;
        }
        log.setTitle(query.title());
        log.setBusinessType(query.businessType());
        log.setBusinessTypes(query.businessTypes());
        log.setStatus(query.status());
        log.setOperName(query.operName());
        log.setOperIp(query.operIp());
        putDateRange(log, query.beginTime(), query.endTime());
        return log;
    }

    private SysLogininfor toEntity(LoginLogQuery query)
    {
        SysLogininfor log = new SysLogininfor();
        if (query == null)
        {
            return log;
        }
        log.setIpaddr(query.ipaddr());
        log.setStatus(query.status());
        log.setUserName(query.userName());
        putDateRange(log, query.beginTime(), query.endTime());
        return log;
    }

    private SysSlowSqlLog toEntity(SlowSqlLogQuery query)
    {
        SysSlowSqlLog log = new SysSlowSqlLog();
        if (query == null)
        {
            return log;
        }
        log.setMapperId(query.mapperId());
        log.setSqlText(query.sqlText());
        log.setDataSourceName(query.dataSourceName());
        log.setCostTime(query.costTime());
        putDateRange(log, query.beginTime(), query.endTime());
        return log;
    }

    private SysMqMessageLog toEntity(MqMessageLogQuery query)
    {
        SysMqMessageLog log = new SysMqMessageLog();
        if (query == null)
        {
            return log;
        }
        log.setMessageType(query.messageType());
        log.setStreamKey(query.streamKey());
        log.setBusinessKey(query.businessKey());
        log.setStatus(query.status());
        putDateRange(log, query.beginTime(), query.endTime());
        return log;
    }

    private void putDateRange(com.manzhushaka.common.core.domain.BaseEntity entity,
            String beginTime, String endTime)
    {
        if (beginTime != null)
        {
            entity.getParams().put("beginTime", beginTime);
        }
        if (endTime != null)
        {
            entity.getParams().put("endTime", endTime);
        }
    }

    private OperLogResult toResult(SysOperLog log)
    {
        if (log == null)
        {
            return null;
        }
        return new OperLogResult(log.getOperId(), log.getTitle(), log.getBusinessType(), log.getMethod(),
                log.getRequestMethod(), log.getOperatorType(), log.getOperName(), log.getDeptName(),
                log.getOperUrl(), log.getOperIp(), log.getOperLocation(), log.getOperParam(), log.getJsonResult(),
                log.getStatus(), log.getErrorMsg(), log.getOperTime(), log.getCostTime());
    }

    private LoginLogResult toResult(SysLogininfor log)
    {
        if (log == null)
        {
            return null;
        }
        return new LoginLogResult(log.getInfoId(), log.getUserName(), log.getStatus(), log.getIpaddr(),
                log.getLoginLocation(), log.getBrowser(), log.getOs(), log.getMsg(), log.getLoginTime());
    }

    private SlowSqlLogResult toResult(SysSlowSqlLog log)
    {
        if (log == null)
        {
            return null;
        }
        return new SlowSqlLogResult(log.getSlowSqlId(), log.getMapperId(), log.getSqlText(),
                log.getDataSourceName(), log.getCostTime(), log.getErrorMsg(), log.getExecuteTime());
    }

    private MqMessageLogResult toResult(SysMqMessageLog log)
    {
        if (log == null)
        {
            return null;
        }
        return new MqMessageLogResult(log.getMessageLogId(), log.getMessageType(), log.getStreamKey(),
                log.getMessageId(), log.getConsumerGroup(), log.getBusinessKey(), log.getPayload(),
                log.getStatus(), log.getRetryTimes(), log.getMaxRetryTimes(), log.getFirstConsumeTime(),
                log.getLastConsumeTime(), log.getSuccessTime(), log.getDeadLetterTime(), log.getLastErrorMsg(),
                log.getCreateBy(), log.getCreateTime(), log.getUpdateBy(), log.getUpdateTime());
    }

    private MqMessageLogDetailResult toResult(SysMqMessageLogDetail detail)
    {
        return new MqMessageLogDetailResult(detail.getDetailId(), detail.getMessageLogId(), detail.getAttemptNo(),
                detail.getConsumerName(), detail.getStatus(), detail.getStartTime(), detail.getEndTime(),
                detail.getCostTime(), detail.getErrorMsg());
    }
}
