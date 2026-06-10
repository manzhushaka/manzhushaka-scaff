package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

import java.time.LocalDateTime;

public class SysJobLog extends BaseEntity {
    private Long jobId;
    private String jobNameSnapshot;
    private String handlerNameSnapshot;
    private String triggerType;
    private String runStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long costMs;
    private String executorHost;
    private String errorMsg;
    private String logContent;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobNameSnapshot() {
        return jobNameSnapshot;
    }

    public void setJobNameSnapshot(String jobNameSnapshot) {
        this.jobNameSnapshot = jobNameSnapshot;
    }

    public String getHandlerNameSnapshot() {
        return handlerNameSnapshot;
    }

    public void setHandlerNameSnapshot(String handlerNameSnapshot) {
        this.handlerNameSnapshot = handlerNameSnapshot;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getCostMs() {
        return costMs;
    }

    public void setCostMs(Long costMs) {
        this.costMs = costMs;
    }

    public String getExecutorHost() {
        return executorHost;
    }

    public void setExecutorHost(String executorHost) {
        this.executorHost = executorHost;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }
}
