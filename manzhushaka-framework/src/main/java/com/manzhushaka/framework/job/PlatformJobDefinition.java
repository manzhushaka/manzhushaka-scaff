package com.manzhushaka.framework.job;

/**
 * 定义 PlatformJobDefinition。
 */
public class PlatformJobDefinition {
    private Long jobId;
    private String handlerName;
    private String cronExpression;
    private Integer status;

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
}
