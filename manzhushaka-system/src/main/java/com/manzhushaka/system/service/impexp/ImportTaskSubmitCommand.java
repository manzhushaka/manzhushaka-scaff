package com.manzhushaka.system.service.impexp;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ImportTaskSubmitCommand {
    private String taskName;
    private String fileName;
    private String contentType;
    @JsonIgnore
    private byte[] content;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonIgnore
    public byte[] getContent() {
        return content == null ? new byte[0] : content.clone();
    }

    public void setContent(byte[] content) {
        this.content = content == null ? new byte[0] : content.clone();
    }
}
