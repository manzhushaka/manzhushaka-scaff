package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

import java.time.LocalDateTime;

/**
 * 映射 SysJobLog 数据库实体。
 */
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

    /**
     * 返回 jobId。
     *
     * @return 字段值
     */
    public Long getJobId() {
        return jobId;
    }

    /**
     * 设置 jobId。
     *
     * @param jobId jobId 标识
     */
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    /**
     * 返回 jobNameSnapshot。
     *
     * @return 字段值
     */
    public String getJobNameSnapshot() {
        return jobNameSnapshot;
    }

    /**
     * 设置 jobNameSnapshot。
     *
     * @param jobNameSnapshot jobNameSnapshot 参数
     */
    public void setJobNameSnapshot(String jobNameSnapshot) {
        this.jobNameSnapshot = jobNameSnapshot;
    }

    /**
     * 返回 handlerNameSnapshot。
     *
     * @return 字段值
     */
    public String getHandlerNameSnapshot() {
        return handlerNameSnapshot;
    }

    /**
     * 设置 handlerNameSnapshot。
     *
     * @param handlerNameSnapshot handlerNameSnapshot 参数
     */
    public void setHandlerNameSnapshot(String handlerNameSnapshot) {
        this.handlerNameSnapshot = handlerNameSnapshot;
    }

    /**
     * 返回 triggerType。
     *
     * @return 字段值
     */
    public String getTriggerType() {
        return triggerType;
    }

    /**
     * 设置 triggerType。
     *
     * @param triggerType triggerType 参数
     */
    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    /**
     * 返回 runStatus。
     *
     * @return 字段值
     */
    public String getRunStatus() {
        return runStatus;
    }

    /**
     * 设置 runStatus。
     *
     * @param runStatus runStatus 参数
     */
    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    /**
     * 返回 startTime。
     *
     * @return 字段值
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * 设置 startTime。
     *
     * @param startTime startTime 参数
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * 返回 endTime。
     *
     * @return 字段值
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * 设置 endTime。
     *
     * @param endTime endTime 参数
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * 返回 costMs。
     *
     * @return 字段值
     */
    public Long getCostMs() {
        return costMs;
    }

    /**
     * 设置 costMs。
     *
     * @param costMs costMs 参数
     */
    public void setCostMs(Long costMs) {
        this.costMs = costMs;
    }

    /**
     * 返回 executorHost。
     *
     * @return 字段值
     */
    public String getExecutorHost() {
        return executorHost;
    }

    /**
     * 设置 executorHost。
     *
     * @param executorHost executorHost 参数
     */
    public void setExecutorHost(String executorHost) {
        this.executorHost = executorHost;
    }

    /**
     * 返回 errorMsg。
     *
     * @return 字段值
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 设置 errorMsg。
     *
     * @param errorMsg errorMsg 参数
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * 返回 logContent。
     *
     * @return 字段值
     */
    public String getLogContent() {
        return logContent;
    }

    /**
     * 设置 logContent。
     *
     * @param logContent logContent 参数
     */
    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }
}
