package com.manzhushaka.system.vo.job;

public class PlatformJobVO {
    private Long id;
    private String jobName;
    private String handlerName;
    private String handlerLabel;
    private String cronExpression;
    private Integer status;
    private String jobParam;
    private String remark;
    private String lastRunStatus;
    private String lastTriggerTime;
    private String nextTriggerTime;
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerLabel() {
        return handlerLabel;
    }

    public void setHandlerLabel(String handlerLabel) {
        this.handlerLabel = handlerLabel;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getJobParam() {
        return jobParam;
    }

    public void setJobParam(String jobParam) {
        this.jobParam = jobParam;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLastRunStatus() {
        return lastRunStatus;
    }

    public void setLastRunStatus(String lastRunStatus) {
        this.lastRunStatus = lastRunStatus;
    }

    public String getLastTriggerTime() {
        return lastTriggerTime;
    }

    public void setLastTriggerTime(String lastTriggerTime) {
        this.lastTriggerTime = lastTriggerTime;
    }

    public String getNextTriggerTime() {
        return nextTriggerTime;
    }

    public void setNextTriggerTime(String nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
