package com.manzhushaka.system.service.impexp;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 定义 ImportTaskSubmitCommand。
 */
public class ImportTaskSubmitCommand {
    private String taskName;
    private String fileName;
    private String contentType;
    @JsonIgnore
    private byte[] content;

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
     * 返回 fileName。
     *
     * @return 字段值
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置 fileName。
     *
     * @param fileName fileName 参数
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 返回 contentType。
     *
     * @return 字段值
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 设置 contentType。
     *
     * @param contentType contentType 参数
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 返回 content。
     *
     * @return 字段值
     */
    @JsonIgnore
    public byte[] getContent() {
        return content == null ? new byte[0] : content.clone();
    }

    /**
     * 设置 content。
     *
     * @param content content 参数
     */
    public void setContent(byte[] content) {
        this.content = content == null ? new byte[0] : content.clone();
    }
}
