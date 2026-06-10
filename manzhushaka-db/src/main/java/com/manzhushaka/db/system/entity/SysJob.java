package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

import java.time.LocalDateTime;

public class SysJob extends BaseEntity {
    private String jobName;
    private String handlerName;
    private String cronExpression;
    private Integer status;
    private String jobParam;
    private String remark;
    private String lastRunStatus;
    private LocalDateTime lastTriggerTime;
    private LocalDateTime nextTriggerTime;

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

    public LocalDateTime getLastTriggerTime() {
        return lastTriggerTime;
    }

    public void setLastTriggerTime(LocalDateTime lastTriggerTime) {
        this.lastTriggerTime = lastTriggerTime;
    }

    public LocalDateTime getNextTriggerTime() {
        return nextTriggerTime;
    }

    public void setNextTriggerTime(LocalDateTime nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }
}
