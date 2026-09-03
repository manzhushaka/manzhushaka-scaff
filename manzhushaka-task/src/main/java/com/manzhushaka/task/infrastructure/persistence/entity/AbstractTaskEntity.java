package com.manzhushaka.task.infrastructure.persistence.entity;

import java.util.Date;

/**
 * 导入导出任务公共持久化字段。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public abstract class AbstractTaskEntity
{
    private Long taskId;
    private String handlerType;
    private String status;
    private String fileKey;
    private String fileName;
    private String contentType;
    private Long requestedBy;
    private Long totalCount;
    private Long processedCount;
    private Long successCount;
    private Long failureCount;
    private String errorMessage;
    private Date startedTime;
    private Date finishedTime;
    private Date leaseUntil;
    private String leaseToken;
    private String securitySnapshot;
    private Date createTime;
    private Date updateTime;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getHandlerType() { return handlerType; }
    public void setHandlerType(String handlerType) { this.handlerType = handlerType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Long requestedBy) { this.requestedBy = requestedBy; }
    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
    public Long getProcessedCount() { return processedCount; }
    public void setProcessedCount(Long processedCount) { this.processedCount = processedCount; }
    public Long getSuccessCount() { return successCount; }
    public void setSuccessCount(Long successCount) { this.successCount = successCount; }
    public Long getFailureCount() { return failureCount; }
    public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Date getStartedTime() { return startedTime; }
    public void setStartedTime(Date startedTime) { this.startedTime = startedTime; }
    public Date getFinishedTime() { return finishedTime; }
    public void setFinishedTime(Date finishedTime) { this.finishedTime = finishedTime; }
    public Date getLeaseUntil() { return leaseUntil; }
    public void setLeaseUntil(Date leaseUntil) { this.leaseUntil = leaseUntil; }
    public String getLeaseToken() { return leaseToken; }
    public void setLeaseToken(String leaseToken) { this.leaseToken = leaseToken; }
    public String getSecuritySnapshot() { return securitySnapshot; }
    public void setSecuritySnapshot(String securitySnapshot) { this.securitySnapshot = securitySnapshot; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[taskId=" + taskId + ", handlerType=" + handlerType
                + ", status=" + status + ", requestedBy=" + requestedBy + ", processedCount="
                + processedCount + ", successCount=" + successCount + ", failureCount=" + failureCount + "]";
    }
}
