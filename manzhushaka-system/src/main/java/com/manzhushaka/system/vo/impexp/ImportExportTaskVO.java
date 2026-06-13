package com.manzhushaka.system.vo.impexp;

import java.time.LocalDateTime;

/**
 * 承载 ImportExportTaskVO 响应数据。
 */
public class ImportExportTaskVO {
    private Long id;
    private String taskNo;
    private String taskType;
    private String bizType;
    private String bizLabel;
    private String taskName;
    private String taskStatus;
    private String taskMessage;
    private String sourceFileName;
    private String resultFileName;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime finishedTime;

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 返回 createBy。
     *
     * @return 字段值
     */
    public String getCreateBy() {
        return createBy;
    }

    /**
     * 设置 createBy。
     *
     * @param createBy createBy 参数
     */
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    /**
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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
