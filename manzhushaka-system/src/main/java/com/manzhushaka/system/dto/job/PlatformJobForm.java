package com.manzhushaka.system.dto.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 承载 PlatformJobForm 请求参数。
 */
public class PlatformJobForm {
    @NotBlank(message = "任务名称不能为空")
    private String jobName;
    @NotBlank(message = "处理器不能为空")
    private String handlerName;
    @NotBlank(message = "Cron 表达式不能为空")
    private String cronExpression;
    @NotNull(message = "状态不能为空")
    private Integer status;
    private String jobParam;
    private String remark;

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
}
