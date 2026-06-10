package com.manzhushaka.system.service.impl;

import com.manzhushaka.db.system.entity.SysJob;
import com.manzhushaka.db.system.mapper.SysJobMapper;
import com.manzhushaka.framework.job.PlatformJobDefinition;
import com.manzhushaka.framework.job.PlatformJobHandler;
import com.manzhushaka.framework.job.PlatformJobHandlerRegistry;
import com.manzhushaka.framework.job.PlatformJobScheduler;
import com.manzhushaka.system.dto.job.PlatformJobForm;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlatformJobServiceImplTest {

    @Test
    void shouldScheduleCreatedJobWhenStatusIsEnabled() {
        SysJobMapper jobMapper = mock(SysJobMapper.class);
        PlatformJobScheduler scheduler = mock(PlatformJobScheduler.class);
        PlatformJobHandlerRegistry handlerRegistry = new PlatformJobHandlerRegistry(List.of(new PlatformJobHandler() {
            @Override
            public String handlerName() {
                return "platformHeartbeatJob";
            }

            @Override
            public String handlerLabel() {
                return "平台心跳任务";
            }
        }));
        doAnswer(invocation -> {
            SysJob entity = invocation.getArgument(0);
            entity.setId(1001L);
            return 1;
        }).when(jobMapper).insert(any(SysJob.class));

        PlatformJobServiceImpl service = new PlatformJobServiceImpl(jobMapper, scheduler, handlerRegistry);
        PlatformJobForm form = new PlatformJobForm();
        form.setJobName("平台心跳");
        form.setHandlerName("platformHeartbeatJob");
        form.setCronExpression("0 0/5 * * * ?");
        form.setStatus(1);
        form.setJobParam("{\"message\":\"hello\"}");

        Long jobId = service.create(form);

        assertEquals(1001L, jobId);
        verify(scheduler).scheduleOrUpdate(any(PlatformJobDefinition.class));
        verify(scheduler).resume(1001L);
    }
}
