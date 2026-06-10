package com.manzhushaka.system.dto.impexp;

import jakarta.validation.constraints.NotBlank;

public class ExportTaskCreateForm {
    @NotBlank(message = "导出场景不能为空")
    private String bizType;
    private String taskName;

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
}
