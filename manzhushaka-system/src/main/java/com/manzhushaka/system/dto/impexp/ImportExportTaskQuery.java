package com.manzhushaka.system.dto.impexp;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 ImportExportTaskQuery 请求参数。
 */
public class ImportExportTaskQuery extends PageQuery {
    private String taskType;
    private String bizType;
    private String taskName;
    private String taskStatus;

    /**
     * 返回 taskType。
     *
     * @return 字段值
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * 设置 taskType。
     *
     * @param taskType taskType 参数
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    /**
     * 返回 bizType。
     *
     * @return 字段值
     */
    public String getBizType() {
        return bizType;
    }

    /**
     * 设置 bizType。
     *
     * @param bizType bizType 参数
     */
    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    /**
     * 返回 taskName。
     *
     * @return 字段值
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * 设置 taskName。
     *
     * @param taskName taskName 参数
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * 返回 taskStatus。
     *
     * @return 字段值
     */
    public String getTaskStatus() {
        return taskStatus;
    }

    /**
     * 设置 taskStatus。
     *
     * @param taskStatus taskStatus 参数
     */
    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
}
