package com.manzhushaka.system.vo.job;

public class PlatformJobLogVO {
    private Long id;
    private Long jobId;
    private String jobNameSnapshot;
    private String handlerNameSnapshot;
    private String triggerType;
    private String runStatus;
    private String executorHost;
    private String errorMsg;
    private Long costMs;
    private String startTime;
    private String endTime;
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getCostMs() {
        return costMs;
    }

    public void setCostMs(Long costMs) {
        this.costMs = costMs;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
