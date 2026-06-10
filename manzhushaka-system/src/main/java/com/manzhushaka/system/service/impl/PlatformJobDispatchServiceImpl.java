package com.manzhushaka.system.service.impl;

import com.manzhushaka.db.system.entity.SysJob;
import com.manzhushaka.db.system.entity.SysJobLog;
import com.manzhushaka.db.system.mapper.SysJobLogMapper;
import com.manzhushaka.db.system.mapper.SysJobMapper;
import com.manzhushaka.framework.job.JobLogger;
import com.manzhushaka.framework.job.PlatformJobDispatchService;
import com.manzhushaka.framework.job.PlatformJobExecutionContext;
import com.manzhushaka.framework.job.PlatformJobHandler;
import com.manzhushaka.framework.job.PlatformJobHandlerRegistry;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlatformJobDispatchServiceImpl implements PlatformJobDispatchService {
    private static final Set<Long> RUNNING_JOB_IDS = ConcurrentHashMap.newKeySet();
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
    private final SysJobMapper jobMapper;
    private final SysJobLogMapper jobLogMapper;
    private final PlatformJobHandlerRegistry handlerRegistry;

    public PlatformJobDispatchServiceImpl(
        SysJobMapper jobMapper,
        SysJobLogMapper jobLogMapper,
        PlatformJobHandlerRegistry handlerRegistry
    ) {
        this.jobMapper = jobMapper;
        this.jobLogMapper = jobLogMapper;
        this.handlerRegistry = handlerRegistry;
    }

    @Override
    public void dispatch(Long jobId, String triggerType, String jobParamOverride) {
        SysJob job = jobMapper.selectById(jobId);
        if (job == null) {
            return;
        }

        String actualTriggerType = StringUtils.hasText(triggerType) ? triggerType : "SCHEDULE";
        String actualJobParam = StringUtils.hasText(jobParamOverride) ? jobParamOverride : job.getJobParam();
        if (!RUNNING_JOB_IDS.add(jobId)) {
            writeSkippedLog(job, actualTriggerType, "任务正在执行中，跳过本次触发。");
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        SysJobLog logEntity = buildRunningLog(job, actualTriggerType, startTime);
        jobLogMapper.insert(logEntity);

        JobLogger.bind(logEntity.getId());
        try {
            JobLogger.info("任务开始执行。");
            PlatformJobHandler handler = handlerRegistry.getRequired(job.getHandlerName());
            PlatformJobExecutionContext context = new PlatformJobExecutionContext();
            context.setJobId(job.getId());
            context.setJobLogId(logEntity.getId());
            context.setJobName(job.getJobName());
            context.setHandlerName(job.getHandlerName());
            context.setTriggerType(actualTriggerType);
            context.setJobParam(actualJobParam);
            handler.execute(context);
            JobLogger.info("任务执行完成。");
            completeLog(job, logEntity, startTime, null);
        } catch (Throwable throwable) {
            JobLogger.error("任务执行失败。", throwable);
            completeLog(job, logEntity, startTime, throwable);
        } finally {
            RUNNING_JOB_IDS.remove(jobId);
        }
    }

    private SysJobLog buildRunningLog(SysJob job, String triggerType, LocalDateTime startTime) {
        SysJobLog log = new SysJobLog();
        log.setJobId(job.getId());
        log.setJobNameSnapshot(job.getJobName());
        log.setHandlerNameSnapshot(job.getHandlerName());
        log.setTriggerType(triggerType);
        log.setRunStatus("RUNNING");
        log.setExecutorHost(resolveExecutorHost());
        log.setStartTime(startTime);
        log.setLogContent("任务已进入执行队列。");
        log.setCreateBy("system");
        log.setUpdateBy("system");
        return log;
    }

    private void completeLog(SysJob job, SysJobLog logEntity, LocalDateTime startTime, Throwable throwable) {
        LocalDateTime endTime = LocalDateTime.now();
        SysJobLog updateLog = new SysJobLog();
        updateLog.setId(logEntity.getId());
        updateLog.setRunStatus(throwable == null ? "SUCCESS" : "FAIL");
        updateLog.setEndTime(endTime);
        updateLog.setCostMs(java.time.Duration.between(startTime, endTime).toMillis());
        updateLog.setErrorMsg(throwable == null ? null : summarizeThrowable(throwable));
        updateLog.setLogContent(JobLogger.collectAndClear());
        updateLog.setUpdateBy("system");
        jobLogMapper.updateById(updateLog);

        SysJob updateJob = new SysJob();
        updateJob.setId(job.getId());
        updateJob.setLastRunStatus(throwable == null ? "SUCCESS" : "FAIL");
        updateJob.setLastTriggerTime(startTime);
        updateJob.setNextTriggerTime(job.getStatus() != null && job.getStatus() == 1 ? nextTriggerTime(job, endTime) : null);
        updateJob.setUpdateBy("system");
        jobMapper.updateById(updateJob);
    }

    private void writeSkippedLog(SysJob job, String triggerType, String message) {
        LocalDateTime now = LocalDateTime.now();
        SysJobLog log = new SysJobLog();
        log.setJobId(job.getId());
        log.setJobNameSnapshot(job.getJobName());
        log.setHandlerNameSnapshot(job.getHandlerName());
        log.setTriggerType(triggerType);
        log.setRunStatus("SKIPPED");
        log.setExecutorHost(resolveExecutorHost());
        log.setErrorMsg(message);
        log.setLogContent(message);
        log.setStartTime(now);
        log.setEndTime(now);
        log.setCostMs(0L);
        log.setCreateBy("system");
        log.setUpdateBy("system");
        jobLogMapper.insert(log);

        SysJob updateJob = new SysJob();
        updateJob.setId(job.getId());
        updateJob.setLastRunStatus("SKIPPED");
        updateJob.setLastTriggerTime(now);
        updateJob.setNextTriggerTime(job.getStatus() != null && job.getStatus() == 1 ? nextTriggerTime(job, now) : null);
        updateJob.setUpdateBy("system");
        jobMapper.updateById(updateJob);
    }

    private LocalDateTime nextTriggerTime(SysJob job, LocalDateTime referenceTime) {
        try {
            CronExpression cronExpression = new CronExpression(job.getCronExpression());
            Date next = cronExpression.getNextValidTimeAfter(Date.from(referenceTime.atZone(SYSTEM_ZONE).toInstant()));
            return next == null ? null : LocalDateTime.ofInstant(next.toInstant(), SYSTEM_ZONE);
        } catch (Exception exception) {
            return null;
        }
    }

    private String resolveExecutorHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "unknown";
        }
    }

    private String summarizeThrowable(Throwable throwable) {
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }
}
