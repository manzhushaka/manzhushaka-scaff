package com.manzhushaka.framework.job;

/**
 * 定义 PlatformJobExecutionContext。
 */
public class PlatformJobExecutionContext {
    private Long jobId;
    private Long jobLogId;
    private String jobName;
    private String handlerName;
    private String triggerType;
    private String jobParam;

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
     * 返回 jobLogId。
     *
     * @return 字段值
     */
    public Long getJobLogId() {
        return jobLogId;
    }

    /**
     * 设置 jobLogId。
     *
     * @param jobLogId jobLogId 标识
     */
    public void setJobLogId(Long jobLogId) {
        this.jobLogId = jobLogId;
    }

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
}
