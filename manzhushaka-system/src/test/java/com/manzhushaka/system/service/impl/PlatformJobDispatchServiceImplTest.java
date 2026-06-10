package com.manzhushaka.system.service.impl;

import com.manzhushaka.db.system.entity.SysJob;
import com.manzhushaka.db.system.entity.SysJobLog;
import com.manzhushaka.db.system.mapper.SysJobLogMapper;
import com.manzhushaka.db.system.mapper.SysJobMapper;
import com.manzhushaka.framework.job.JobLogger;
import com.manzhushaka.framework.job.PlatformJobExecutionContext;
import com.manzhushaka.framework.job.PlatformJobHandler;
import com.manzhushaka.framework.job.PlatformJobHandlerRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformJobDispatchServiceImplTest {

    @Test
    void shouldPersistExecutionLogContentAndSuccessStatus() {
        SysJobMapper jobMapper = mock(SysJobMapper.class);
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJob job = new SysJob();
        job.setId(11L);
        job.setJobName("平台心跳");
        job.setHandlerName("platformHeartbeatJob");
        job.setCronExpression("0 0/5 * * * ?");
        when(jobMapper.selectById(11L)).thenReturn(job);

        AtomicReference<SysJobLog> updatedLog = new AtomicReference<>();
        AtomicReference<SysJob> updatedJob = new AtomicReference<>();
        doAnswer(invocation -> {
            SysJobLog entity = invocation.getArgument(0);
            entity.setId(9001L);
            return 1;
        }).when(jobLogMapper).insert(any(SysJobLog.class));
        doAnswer(invocation -> {
            updatedLog.set(invocation.getArgument(0));
            return 1;
        }).when(jobLogMapper).updateById(any(SysJobLog.class));
        doAnswer(invocation -> {
            updatedJob.set(invocation.getArgument(0));
            return 1;
        }).when(jobMapper).updateById(any(SysJob.class));

        PlatformJobHandler handler = new PlatformJobHandler() {
            @Override
            public String handlerName() {
                return "platformHeartbeatJob";
            }

            @Override
            public String handlerLabel() {
                return "平台心跳任务";
            }

            @Override
            public void execute(PlatformJobExecutionContext context) {
                JobLogger.info("开始执行任务 {}", context.getJobName());
                JobLogger.info("处理参数 {}", context.getJobParam());
            }
        };

        PlatformJobDispatchServiceImpl service = new PlatformJobDispatchServiceImpl(
            jobMapper,
            jobLogMapper,
            new PlatformJobHandlerRegistry(List.of(handler))
        );

        service.dispatch(11L, "MANUAL", "{\"message\":\"hello\"}");

        verify(jobLogMapper).insert(any(SysJobLog.class));
        assertNotNull(updatedLog.get());
        assertEquals("SUCCESS", updatedLog.get().getRunStatus());
        assertTrue(updatedLog.get().getLogContent().contains("开始执行任务 平台心跳"));
        assertTrue(updatedLog.get().getLogContent().contains("处理参数 {\"message\":\"hello\"}"));
        assertNotNull(updatedJob.get());
        assertEquals("SUCCESS", updatedJob.get().getLastRunStatus());
    }
}
