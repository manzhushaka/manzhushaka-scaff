package com.manzhushaka.system.vo.job;

/**
 * 承载 PlatformJobVO 响应数据。
 */
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
     * 返回 handlerLabel。
     *
     * @return 字段值
     */
    public String getHandlerLabel() {
        return handlerLabel;
    }

    /**
     * 设置 handlerLabel。
     *
     * @param handlerLabel handlerLabel 参数
     */
    public void setHandlerLabel(String handlerLabel) {
        this.handlerLabel = handlerLabel;
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
    public String getLastTriggerTime() {
        return lastTriggerTime;
    }

    /**
     * 设置 lastTriggerTime。
     *
     * @param lastTriggerTime lastTriggerTime 参数
     */
    public void setLastTriggerTime(String lastTriggerTime) {
        this.lastTriggerTime = lastTriggerTime;
    }

    /**
     * 返回 nextTriggerTime。
     *
     * @return 字段值
     */
    public String getNextTriggerTime() {
        return nextTriggerTime;
    }

    /**
     * 设置 nextTriggerTime。
     *
     * @param nextTriggerTime nextTriggerTime 参数
     */
    public void setNextTriggerTime(String nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
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
