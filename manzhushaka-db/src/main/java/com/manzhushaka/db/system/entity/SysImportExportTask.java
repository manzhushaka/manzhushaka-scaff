package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

import java.time.LocalDateTime;

public class SysImportExportTask extends BaseEntity {
    private String taskNo;
    private String taskType;
    private String bizType;
    private String bizLabel;
    private String taskName;
    private String taskStatus;
    private String taskMessage;
    private String taskParam;
    private String sourceFileName;
    private String sourceObjectKey;
    private Long sourceFileSize;
    private String resultFileName;
    private String resultObjectKey;
    private Long resultFileSize;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private LocalDateTime finishedTime;

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

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

    public String getBizLabel() {
        return bizLabel;
    }

    public void setBizLabel(String bizLabel) {
        this.bizLabel = bizLabel;
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

    public String getTaskMessage() {
        return taskMessage;
    }

    public void setTaskMessage(String taskMessage) {
        this.taskMessage = taskMessage;
    }

    public String getTaskParam() {
        return taskParam;
    }

    public void setTaskParam(String taskParam) {
        this.taskParam = taskParam;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getSourceObjectKey() {
        return sourceObjectKey;
    }

    public void setSourceObjectKey(String sourceObjectKey) {
        this.sourceObjectKey = sourceObjectKey;
    }

    public Long getSourceFileSize() {
        return sourceFileSize;
    }

    public void setSourceFileSize(Long sourceFileSize) {
        this.sourceFileSize = sourceFileSize;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public String getResultObjectKey() {
        return resultObjectKey;
    }

    public void setResultObjectKey(String resultObjectKey) {
        this.resultObjectKey = resultObjectKey;
    }

    public Long getResultFileSize() {
        return resultFileSize;
    }

    public void setResultFileSize(Long resultFileSize) {
        this.resultFileSize = resultFileSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public LocalDateTime getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(LocalDateTime finishedTime) {
        this.finishedTime = finishedTime;
    }
}
