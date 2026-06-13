package com.manzhushaka.system.dto.job;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 PlatformJobQuery 请求参数。
 */
public class PlatformJobQuery extends PageQuery {
    private String jobName;
    private String handlerName;
    private Integer status;
    private String lastRunStatus;

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
}
