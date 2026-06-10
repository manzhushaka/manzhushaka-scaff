package com.manzhushaka.framework.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class PlatformQuartzDispatchJob implements Job {

    private final PlatformJobDispatchService dispatchService;

    public PlatformQuartzDispatchJob(PlatformJobDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        Long jobId = context.getMergedJobDataMap().getLong("jobId");
        String triggerType = context.getMergedJobDataMap().getString("triggerType");
        dispatchService.dispatch(jobId, triggerType == null ? "SCHEDULE" : triggerType, null);
    }
}
