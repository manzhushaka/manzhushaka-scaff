package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

import java.time.LocalDateTime;

/**
 * 映射 SysJob 数据库实体。
 */
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

    /**
     * 返回 jobName。
     *
     * @return 字段值
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * 设置 jobName。
     *
     * @param jobName jobName 参数
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * 返回 handlerName。
     *
     * @return 字段值
     */
    public String getHandlerName() {
        return handlerName;
    }

    /**
     * 设置 handlerName。
     *
     * @param handlerName handlerName 参数
     */
    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    /**
     * 返回 cronExpression。
     *
     * @return 字段值
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * 设置 cronExpression。
     *
     * @param cronExpression cronExpression 参数
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    /**
     * 返回 status。
     *
     * @return 字段值
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status status 参数
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 返回 jobParam。
     *
     * @return 字段值
     */
    public String getJobParam() {
        return jobParam;
    }

    /**
     * 设置 jobParam。
     *
     * @param jobParam jobParam 参数
     */
    public void setJobParam(String jobParam) {
        this.jobParam = jobParam;
    }

    /**
     * 返回 remark。
     *
     * @return 字段值
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置 remark。
     *
     * @param remark remark 参数
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 返回 lastRunStatus。
     *
     * @return 字段值
     */
    public String getLastRunStatus() {
        return lastRunStatus;
    }

    /**
     * 设置 lastRunStatus。
     *
     * @param lastRunStatus lastRunStatus 参数
     */
    public void setLastRunStatus(String lastRunStatus) {
        this.lastRunStatus = lastRunStatus;
    }

    /**
     * 返回 lastTriggerTime。
     *
     * @return 字段值
     */
    public LocalDateTime getLastTriggerTime() {
        return lastTriggerTime;
    }

    /**
     * 设置 lastTriggerTime。
     *
     * @param lastTriggerTime lastTriggerTime 参数
     */
    public void setLastTriggerTime(LocalDateTime lastTriggerTime) {
        this.lastTriggerTime = lastTriggerTime;
    }

    /**
     * 返回 nextTriggerTime。
     *
     * @return 字段值
     */
    public LocalDateTime getNextTriggerTime() {
        return nextTriggerTime;
    }

    /**
     * 设置 nextTriggerTime。
     *
     * @param nextTriggerTime nextTriggerTime 参数
     */
    public void setNextTriggerTime(LocalDateTime nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }
}
