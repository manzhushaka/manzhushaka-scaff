package com.manzhushaka.framework.job;

import com.manzhushaka.common.exception.BizException;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

/**
 * 定义 QuartzPlatformJobScheduler。
 */
@Component
public class QuartzPlatformJobScheduler implements PlatformJobScheduler {
    private static final String JOB_GROUP = "platform-job";
    private static final String TRIGGER_GROUP = "platform-job-trigger";
    private final Scheduler scheduler;

    /**
     * 创建 QuartzPlatformJobScheduler 实例。
     *
     * @param scheduler scheduler 参数
     */
    public QuartzPlatformJobScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 执行 schedule Or Update 逻辑。
     *
     * @param definition definition 参数
     */
    @Override
    public void scheduleOrUpdate(PlatformJobDefinition definition) {
        try {
            JobKey jobKey = buildJobKey(definition.getJobId());
            TriggerKey triggerKey = buildTriggerKey(definition.getJobId());
            JobDetail jobDetail = JobBuilder.newJob(PlatformQuartzDispatchJob.class)
                .withIdentity(jobKey)
                .usingJobData(buildJobData(definition.getJobId()))
                .storeDurably()
                .build();
            scheduler.addJob(jobDetail, true);

            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobKey)
                .usingJobData("triggerType", "SCHEDULE")
                .withSchedule(
                    CronScheduleBuilder.cronSchedule(definition.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing()
                )
                .build();

            if (scheduler.checkExists(triggerKey)) {
                scheduler.rescheduleJob(triggerKey, cronTrigger);
            } else {
                scheduler.scheduleJob(cronTrigger);
            }
        } catch (SchedulerException exception) {
            throw new BizException(500, "同步 Quartz 任务失败");
        }
    }

    /**
     * 删除数据。
     *
     * @param jobId jobId 标识
     */
    @Override
    public void delete(Long jobId) {
        try {
            scheduler.deleteJob(buildJobKey(jobId));
        } catch (SchedulerException exception) {
            throw new BizException(500, "删除 Quartz 任务失败");
        }
    }

    /**
     * 暂停任务。
     *
     * @param jobId jobId 标识
     */
    @Override
    public void pause(Long jobId) {
        try {
            scheduler.pauseJob(buildJobKey(jobId));
        } catch (SchedulerException exception) {
            throw new BizException(500, "暂停 Quartz 任务失败");
        }
    }

    /**
     * 恢复任务。
     *
     * @param jobId jobId 标识
     */
    @Override
    public void resume(Long jobId) {
        try {
            scheduler.resumeJob(buildJobKey(jobId));
        } catch (SchedulerException exception) {
            throw new BizException(500, "恢复 Quartz 任务失败");
        }
    }

    /**
     * 执行 trigger Now 操作。
     *
     * @param jobId jobId 标识
     */
    @Override
    public void triggerNow(Long jobId) {
        try {
            scheduler.triggerJob(buildJobKey(jobId), new JobDataMap(buildJobData(jobId, "MANUAL")));
        } catch (SchedulerException exception) {
            throw new BizException(500, "触发 Quartz 任务失败");
        }
    }

    /**
     * 构建 build Job Data 结果。
     *
     * @param jobId jobId 标识
     * @return 处理结果
     */
    private JobDataMap buildJobData(Long jobId) {
        return buildJobData(jobId, "SCHEDULE");
    }

    /**
     * 构建 build Job Data 结果。
     *
     * @param jobId jobId 标识
     * @param triggerType triggerType 参数
     * @return 处理结果
     */
    private JobDataMap buildJobData(Long jobId, String triggerType) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobId", jobId);
        jobDataMap.put("triggerType", triggerType);
        return jobDataMap;
    }

    /**
     * 构建 build Job Key 结果。
     *
     * @param jobId jobId 标识
     * @return 处理结果
     */
    private JobKey buildJobKey(Long jobId) {
        return JobKey.jobKey("platform-job-" + jobId, JOB_GROUP);
    }

    /**
     * 构建 build Trigger Key 结果。
     *
     * @param jobId jobId 标识
     * @return 处理结果
     */
    private TriggerKey buildTriggerKey(Long jobId) {
        return TriggerKey.triggerKey("platform-job-trigger-" + jobId, TRIGGER_GROUP);
    }
}
