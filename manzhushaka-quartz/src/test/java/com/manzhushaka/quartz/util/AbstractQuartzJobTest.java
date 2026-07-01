package com.manzhushaka.quartz.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import com.manzhushaka.common.constant.ScheduleConstants;
import com.manzhushaka.common.utils.JobLog;
import com.manzhushaka.common.utils.spring.SpringUtils;
import com.manzhushaka.quartz.domain.SysJob;
import com.manzhushaka.quartz.domain.SysJobLog;
import com.manzhushaka.quartz.domain.SysJobLogDetail;
import com.manzhushaka.quartz.service.ISysJobLogService;

/**
 * Quartz 任务过程日志测试。
 *
 * @author manzhushaka
 * @date 2026-07-01
 */
class AbstractQuartzJobTest
{
    @Test
    @SuppressWarnings("unchecked")
    void executeShouldPersistCollectedProcessLogs() throws Exception
    {
        ISysJobLogService jobLogService = mock(ISysJobLogService.class);
        org.mockito.Mockito.doAnswer(invocation -> {
            SysJobLog jobLog = invocation.getArgument(0);
            jobLog.setJobLogId(100L);
            return jobLog;
        }).when(jobLogService).addJobLog(any(SysJobLog.class));
        JobExecutionContext context = mock(JobExecutionContext.class);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ScheduleConstants.TASK_PROPERTIES, createSysJob());
        when(context.getMergedJobDataMap()).thenReturn(jobDataMap);

        try (MockedStatic<SpringUtils> springUtils = mockStatic(SpringUtils.class))
        {
            springUtils.when(() -> SpringUtils.getBean(ISysJobLogService.class)).thenReturn(jobLogService);

            new AbstractQuartzJob()
            {
                @Override
                protected void doExecute(JobExecutionContext context, SysJob sysJob)
                {
                    JobLog.info("开始执行：{}", sysJob.getJobName());
                    JobLog.warn("发现慢步骤");
                    JobLog.error("业务错误信息");
                }
            }.execute(context);
        }

        ArgumentCaptor<List<SysJobLogDetail>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(jobLogService).addJobLogDetails(detailsCaptor.capture());
        List<SysJobLogDetail> details = detailsCaptor.getValue();
        assertThat(details).hasSize(3);
        assertThat(details.get(0).getJobLogId()).isEqualTo(100L);
        assertThat(details.get(0).getLogLevel()).isEqualTo("INFO");
        assertThat(details.get(0).getLogContent()).isEqualTo("开始执行：测试任务");
        assertThat(details.get(0).getSortNo()).isEqualTo(1);
        assertThat(details.get(1).getLogLevel()).isEqualTo("WARN");
        assertThat(details.get(2).getLogLevel()).isEqualTo("ERROR");
    }

    /**
     * 创建测试任务。
     *
     * @return 测试任务
     */
    private SysJob createSysJob()
    {
        SysJob sysJob = new SysJob();
        sysJob.setJobName("测试任务");
        sysJob.setJobGroup("DEFAULT");
        sysJob.setInvokeTarget("scaffTask.scaffNoParams");
        return sysJob;
    }
}
