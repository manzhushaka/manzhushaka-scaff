package com.manzhushaka.framework.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * 定义 PlatformQuartzDispatchJob。
 */
public class PlatformQuartzDispatchJob implements Job {

    private final PlatformJobDispatchService dispatchService;

    /**
     * 创建 PlatformQuartzDispatchJob 实例。
     *
     * @param dispatchService dispatchService 参数
     */
    public PlatformQuartzDispatchJob(PlatformJobDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    /**
     * 执行任务处理。
     *
     * @param context 执行上下文
     */
    @Override
    public void execute(JobExecutionContext context) {
        Long jobId = context.getMergedJobDataMap().getLong("jobId");
        String triggerType = context.getMergedJobDataMap().getString("triggerType");
        dispatchService.dispatch(jobId, triggerType == null ? "SCHEDULE" : triggerType, null);
    }
}
