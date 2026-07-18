package com.manzhushaka.web.converter.monitor;

import com.manzhushaka.system.application.query.LoginLogQuery;
import com.manzhushaka.system.application.query.MqMessageLogQuery;
import com.manzhushaka.system.application.query.OperLogQuery;
import com.manzhushaka.system.application.query.SlowSqlLogQuery;
import com.manzhushaka.web.dto.monitor.LoginLogQueryRequest;
import com.manzhushaka.web.dto.monitor.MqMessageLogQueryRequest;
import com.manzhushaka.web.dto.monitor.OperLogQueryRequest;
import com.manzhushaka.web.dto.monitor.SlowSqlLogQueryRequest;

/**
 * 系统审计 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class AuditAdminConverter
{
    private AuditAdminConverter()
    {
    }

    public static OperLogQuery toQuery(OperLogQueryRequest request)
    {
        return new OperLogQuery(request.getTitle(), request.getBusinessType(), request.getBusinessTypes(),
                request.getStatus(), request.getOperName(), request.getOperIp(),
                request.getBeginTime(), request.getEndTime());
    }

    public static LoginLogQuery toQuery(LoginLogQueryRequest request)
    {
        return new LoginLogQuery(request.getIpaddr(), request.getStatus(), request.getUserName(),
                request.getBeginTime(), request.getEndTime());
    }

    public static SlowSqlLogQuery toQuery(SlowSqlLogQueryRequest request)
    {
        return new SlowSqlLogQuery(request.getMapperId(), request.getSqlText(), request.getDataSourceName(),
                request.getCostTime(), request.getBeginTime(), request.getEndTime());
    }

    public static MqMessageLogQuery toQuery(MqMessageLogQueryRequest request)
    {
        return new MqMessageLogQuery(request.getMessageType(), request.getStreamKey(), request.getBusinessKey(),
                request.getStatus(), request.getBeginTime(), request.getEndTime());
    }
}
