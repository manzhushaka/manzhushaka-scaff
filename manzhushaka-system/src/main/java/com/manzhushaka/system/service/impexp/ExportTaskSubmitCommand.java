package com.manzhushaka.system.service.impexp;

/**
 * 定义 ExportTaskSubmitCommand。
 */
public class ExportTaskSubmitCommand {
    private String taskName;

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
}
