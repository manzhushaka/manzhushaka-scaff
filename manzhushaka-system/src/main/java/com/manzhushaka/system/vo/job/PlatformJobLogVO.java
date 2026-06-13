package com.manzhushaka.system.vo.job;

/**
 * 承载 PlatformJobLogVO 响应数据。
 */
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

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 返回 startTime。
     *
     * @return 字段值
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * 设置 startTime。
     *
     * @param startTime startTime 参数
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * 返回 endTime。
     *
     * @return 字段值
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * 设置 endTime。
     *
     * @param endTime endTime 参数
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * 返回 createTime。
     *
     * @return 字段值
     */
    public String getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
