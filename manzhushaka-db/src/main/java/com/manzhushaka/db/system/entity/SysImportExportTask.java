package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

import java.time.LocalDateTime;

/**
 * 映射 SysImportExportTask 数据库实体。
 */
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

    /**
     * 返回 taskNo。
     *
     * @return 字段值
     */
    public String getTaskNo() {
        return taskNo;
    }

    /**
     * 设置 taskNo。
     *
     * @param taskNo taskNo 参数
     */
    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

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
     * 返回 bizLabel。
     *
     * @return 字段值
     */
    public String getBizLabel() {
        return bizLabel;
    }

    /**
     * 设置 bizLabel。
     *
     * @param bizLabel bizLabel 参数
     */
    public void setBizLabel(String bizLabel) {
        this.bizLabel = bizLabel;
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

    /**
     * 返回 taskMessage。
     *
     * @return 字段值
     */
    public String getTaskMessage() {
        return taskMessage;
    }

    /**
     * 设置 taskMessage。
     *
     * @param taskMessage taskMessage 参数
     */
    public void setTaskMessage(String taskMessage) {
        this.taskMessage = taskMessage;
    }

    /**
     * 返回 taskParam。
     *
     * @return 字段值
     */
    public String getTaskParam() {
        return taskParam;
    }

    /**
     * 设置 taskParam。
     *
     * @param taskParam taskParam 参数
     */
    public void setTaskParam(String taskParam) {
        this.taskParam = taskParam;
    }

    /**
     * 返回 sourceFileName。
     *
     * @return 字段值
     */
    public String getSourceFileName() {
        return sourceFileName;
    }

    /**
     * 设置 sourceFileName。
     *
     * @param sourceFileName sourceFileName 参数
     */
    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    /**
     * 返回 sourceObjectKey。
     *
     * @return 字段值
     */
    public String getSourceObjectKey() {
        return sourceObjectKey;
    }

    /**
     * 设置 sourceObjectKey。
     *
     * @param sourceObjectKey sourceObjectKey 参数
     */
    public void setSourceObjectKey(String sourceObjectKey) {
        this.sourceObjectKey = sourceObjectKey;
    }

    /**
     * 返回 sourceFileSize。
     *
     * @return 字段值
     */
    public Long getSourceFileSize() {
        return sourceFileSize;
    }

    /**
     * 设置 sourceFileSize。
     *
     * @param sourceFileSize sourceFileSize 参数
     */
    public void setSourceFileSize(Long sourceFileSize) {
        this.sourceFileSize = sourceFileSize;
    }

    /**
     * 返回 resultFileName。
     *
     * @return 字段值
     */
    public String getResultFileName() {
        return resultFileName;
    }

    /**
     * 设置 resultFileName。
     *
     * @param resultFileName resultFileName 参数
     */
    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    /**
     * 返回 resultObjectKey。
     *
     * @return 字段值
     */
    public String getResultObjectKey() {
        return resultObjectKey;
    }

    /**
     * 设置 resultObjectKey。
     *
     * @param resultObjectKey resultObjectKey 参数
     */
    public void setResultObjectKey(String resultObjectKey) {
        this.resultObjectKey = resultObjectKey;
    }

    /**
     * 返回 resultFileSize。
     *
     * @return 字段值
     */
    public Long getResultFileSize() {
        return resultFileSize;
    }

    /**
     * 设置 resultFileSize。
     *
     * @param resultFileSize resultFileSize 参数
     */
    public void setResultFileSize(Long resultFileSize) {
        this.resultFileSize = resultFileSize;
    }

    /**
     * 返回 totalCount。
     *
     * @return 字段值
     */
    public Integer getTotalCount() {
        return totalCount;
    }

    /**
     * 设置 totalCount。
     *
     * @param totalCount totalCount 参数
     */
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 返回 successCount。
     *
     * @return 字段值
     */
    public Integer getSuccessCount() {
        return successCount;
    }

    /**
     * 设置 successCount。
     *
     * @param successCount successCount 参数
     */
    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    /**
     * 返回 failCount。
     *
     * @return 字段值
     */
    public Integer getFailCount() {
        return failCount;
    }

    /**
     * 设置 failCount。
     *
     * @param failCount failCount 参数
     */
    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    /**
     * 返回 finishedTime。
     *
     * @return 字段值
     */
    public LocalDateTime getFinishedTime() {
        return finishedTime;
    }

    /**
     * 设置 finishedTime。
     *
     * @param finishedTime finishedTime 参数
     */
    public void setFinishedTime(LocalDateTime finishedTime) {
        this.finishedTime = finishedTime;
    }
}
