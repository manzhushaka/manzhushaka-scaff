package com.manzhushaka.system.service.impl;

import com.manzhushaka.db.monitor.SlowSqlMonitorStore;
import com.manzhushaka.db.system.entity.SysJob;
import com.manzhushaka.db.system.entity.SysJobLog;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysJobLogMapper;
import com.manzhushaka.db.system.mapper.SysJobMapper;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.framework.monitor.ApplicationLogBuffer;
import com.manzhushaka.system.vo.monitor.ServerMonitorVO;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerMonitorServiceMonitorOpsTest {

    /**
     * 验证运行监控服务会聚合任务、消息、慢 SQL 和在线日志摘要。
     */
    @Test
    void shouldBuildOperationsMonitorSummary() {
        Environment environment = mock(Environment.class);
        when(environment.getProperty("spring.application.name", "application")).thenReturn("manzhushaka-admin");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});

        SysJobMapper jobMapper = mock(SysJobMapper.class);
        when(jobMapper.selectList(any())).thenReturn(List.of(
            job(1L, "平台心跳", 1, "SUCCESS"),
            job(2L, "消息补偿", 1, "FAIL"),
            job(3L, "缓存巡检", 0, "SUCCESS")
        ));

        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        when(jobLogMapper.selectList(any())).thenReturn(List.of(
            jobLog(11L, 2L, "消息补偿", "FAIL", LocalDateTime.now().minusMinutes(8), "重试失败"),
            jobLog(12L, 1L, "平台心跳", "SUCCESS", LocalDateTime.now().minusMinutes(6), null),
            jobLog(13L, 2L, "消息补偿", "FAIL", LocalDateTime.now().minusMinutes(3), "连接超时")
        ));

        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        when(mqMessageMapper.selectList(any())).thenReturn(List.of(
            mqMessage(101L, "evt-init", "INIT", LocalDateTime.now().minusMinutes(20), null, null),
            mqMessage(102L, "evt-published", "PUBLISHED", LocalDateTime.now().minusMinutes(18), null, LocalDateTime.now().minusMinutes(15)),
            mqMessage(103L, "evt-processing", "PROCESSING", LocalDateTime.now().minusMinutes(12), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(10)),
            mqMessage(104L, "evt-fail", "FAIL", LocalDateTime.now().minusMinutes(5), null, null),
            mqMessage(105L, "evt-success", "SUCCESS", LocalDateTime.now().minusMinutes(2), null, null)
        ));

        SlowSqlMonitorStore slowSqlMonitorStore = new SlowSqlMonitorStore(8);
        slowSqlMonitorStore.record("com.manzhushaka.db.system.mapper.UserMapper.selectPage", "SELECT * FROM sys_user", 188L, 20);

        ApplicationLogBuffer logBuffer = new ApplicationLogBuffer(20);
        logBuffer.append(LocalDateTime.now().minusSeconds(3), "INFO monitor tab ready");
        logBuffer.append(LocalDateTime.now().minusSeconds(1), "WARN redis backlog rising");

        ServerMonitorService service = new ServerMonitorService(
            new SimpleMeterRegistry(),
            environment,
            mock(RedisConnectionFactory.class),
            jobMapper,
            jobLogMapper,
            mqMessageMapper,
            slowSqlMonitorStore,
            logBuffer
        );

        ServerMonitorVO result = service.getServerMonitor();

        assertEquals(3L, result.getJobHealth().getTotalJobs());
        assertEquals(2L, result.getJobHealth().getEnabledJobs());
        assertEquals(1L, result.getJobHealth().getPausedJobs());
        assertEquals(1L, result.getJobHealth().getRecentSuccessCount());
        assertEquals(2L, result.getJobHealth().getRecentFailCount());
        assertTrue(result.getJobHealth().getRecentFailures().size() >= 1);
        assertEquals(3L, result.getMessageBacklog().getPendingCount());
        assertEquals(1L, result.getMessageBacklog().getFailCount());
        assertEquals(2L, result.getMessageBacklog().getTimedOutCount());
        assertEquals("evt-init", result.getMessageBacklog().getOldestPendingEventId());
        assertEquals(1, result.getSlowSql().getRecentCount());
        assertEquals(188L, result.getSlowSql().getLatestCostMs());
        assertEquals(2, result.getLogTail().getEntryCount());
        assertTrue(result.getLogTail().getAvailable());
        assertEquals(1, service.listSlowSqlRecords(10).size());
        assertEquals(2, service.getLogTail(10).getLines().size());
    }

    /**
     * 构造任务实体样本。
     *
     * @param id 任务主键
     * @param jobName 任务名称
     * @param status 任务状态
     * @param lastRunStatus 最近执行状态
     * @return SysJob
     */
    private SysJob job(Long id, String jobName, Integer status, String lastRunStatus) {
        SysJob entity = new SysJob();
        entity.setId(id);
        entity.setJobName(jobName);
        entity.setStatus(status);
        entity.setLastRunStatus(lastRunStatus);
        return entity;
    }

    /**
     * 构造任务日志实体样本。
     *
     * @param id 日志主键
     * @param jobId 任务主键
     * @param jobName 任务名称快照
     * @param runStatus 执行结果
     * @param startTime 开始时间
     * @param errorMsg 错误信息
     * @return SysJobLog
     */
    private SysJobLog jobLog(Long id, Long jobId, String jobName, String runStatus, LocalDateTime startTime, String errorMsg) {
        SysJobLog entity = new SysJobLog();
        entity.setId(id);
        entity.setJobId(jobId);
        entity.setJobNameSnapshot(jobName);
        entity.setRunStatus(runStatus);
        entity.setStartTime(startTime);
        entity.setErrorMsg(errorMsg);
        return entity;
    }

    /**
     * 构造消息台账实体样本。
     *
     * @param id 主键
     * @param eventId 事件编号
     * @param status 消息状态
     * @param createTime 创建时间
     * @param processingDeadlineAt 处理截止时间
     * @param publishedAt 发布时间
     * @return SysMqMessage
     */
    private SysMqMessage mqMessage(
        Long id,
        String eventId,
        String status,
        LocalDateTime createTime,
        LocalDateTime processingDeadlineAt,
        LocalDateTime publishedAt
    ) {
        SysMqMessage entity = new SysMqMessage();
        entity.setId(id);
        entity.setEventId(eventId);
        entity.setStreamKey("stream:oplog");
        entity.setStatus(status);
        entity.setCreateTime(createTime);
        entity.setProcessingDeadlineAt(processingDeadlineAt);
        entity.setPublishedAt(publishedAt);
        return entity;
    }
}
