package com.manzhushaka.system.dto.impexp;

import com.manzhushaka.system.dto.PageQuery;

public class ImportExportTaskQuery extends PageQuery {
    private String taskType;
    private String bizType;
    private String taskName;
    private String taskStatus;

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
}
