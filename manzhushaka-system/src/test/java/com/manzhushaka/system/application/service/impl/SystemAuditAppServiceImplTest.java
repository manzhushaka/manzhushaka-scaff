package com.manzhushaka.system.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.system.application.query.SlowSqlLogQuery;
import com.manzhushaka.system.application.result.system.SlowSqlLogResult;
import com.manzhushaka.system.domain.SysSlowSqlLog;
import com.manzhushaka.system.service.ISysLogininforService;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import com.manzhushaka.system.service.ISysOperLogService;
import com.manzhushaka.system.service.ISysSlowSqlLogService;

/**
 * 系统审计应用服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class SystemAuditAppServiceImplTest
{
    /** 慢 SQL 查询应保留筛选条件、日期范围和返回字段。 */
    @Test
    void listSlowSqlLogsShouldPreserveQueryAndResultContract()
    {
        SystemAuditAppServiceImpl service = buildService();
        ISysSlowSqlLogService slowSqlService = (ISysSlowSqlLogService) ReflectionTestUtils.getField(
                service, "slowSqlLogService");
        Date executeTime = new Date();
        SysSlowSqlLog entity = new SysSlowSqlLog();
        entity.setSlowSqlId(10L);
        entity.setMapperId("SysUserMapper.selectUserList");
        entity.setSqlText("select * from sys_user");
        entity.setDataSourceName("MASTER");
        entity.setCostTime(800L);
        entity.setExecuteTime(executeTime);
        ArgumentCaptor<SysSlowSqlLog> queryCaptor = ArgumentCaptor.forClass(SysSlowSqlLog.class);
        when(slowSqlService.selectSlowSqlLogList(queryCaptor.capture())).thenReturn(List.of(entity));

        List<SlowSqlLogResult> results = service.listSlowSqlLogs(new SlowSqlLogQuery(
                "SysUserMapper", "sys_user", "MASTER", 500L,
                "2026-07-01 00:00:00", "2026-07-18 23:59:59"));

        SysSlowSqlLog query = queryCaptor.getValue();
        assertThat(query.getMapperId()).isEqualTo("SysUserMapper");
        assertThat(query.getSqlText()).isEqualTo("sys_user");
        assertThat(query.getDataSourceName()).isEqualTo("MASTER");
        assertThat(query.getCostTime()).isEqualTo(500L);
        assertThat(query.getParams()).containsEntry("beginTime", "2026-07-01 00:00:00")
                .containsEntry("endTime", "2026-07-18 23:59:59");
        assertThat(results).containsExactly(new SlowSqlLogResult(10L,
                "SysUserMapper.selectUserList", "select * from sys_user", "MASTER",
                800L, null, executeTime));
    }

    /**
     * 构建测试服务。
     *
     * @return 系统审计应用服务
     */
    private SystemAuditAppServiceImpl buildService()
    {
        SystemAuditAppServiceImpl service = new SystemAuditAppServiceImpl();
        ReflectionTestUtils.setField(service, "logininforService", mock(ISysLogininforService.class));
        ReflectionTestUtils.setField(service, "operLogService", mock(ISysOperLogService.class));
        ReflectionTestUtils.setField(service, "slowSqlLogService", mock(ISysSlowSqlLogService.class));
        ReflectionTestUtils.setField(service, "mqMessageLogService", mock(ISysMqMessageLogService.class));
        return service;
    }
}
