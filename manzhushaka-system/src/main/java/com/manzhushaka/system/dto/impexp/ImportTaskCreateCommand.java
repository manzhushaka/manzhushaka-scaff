package com.manzhushaka.system.dto.impexp;

public class ImportTaskCreateCommand {
    private final String bizType;
    private final String taskName;
    private final String fileName;
    private final String contentType;
    private final byte[] content;

    public ImportTaskCreateCommand(String bizType, String taskName, String fileName, String contentType, byte[] content) {
        this.bizType = bizType;
        this.taskName = taskName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content == null ? new byte[0] : content.clone();
    }

    public String getBizType() {
        return bizType;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content.clone();
    }
}
