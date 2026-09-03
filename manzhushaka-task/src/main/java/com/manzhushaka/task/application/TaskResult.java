package com.manzhushaka.task.application;

import java.util.Date;

import com.manzhushaka.task.infrastructure.persistence.entity.AbstractTaskEntity;

/**
 * 可安全返回给前端的任务信息。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public record TaskResult(Long taskId, String handlerType, String status, String fileName,
        Long requestedBy, Long totalCount, Long processedCount, Long successCount,
        Long failureCount, String errorMessage, Date startedTime, Date finishedTime,
        Date createTime, Date updateTime)
{
    /** 从持久化实体创建前端结果。 */
    public static TaskResult from(AbstractTaskEntity task)
    {
        return new TaskResult(task.getTaskId(), task.getHandlerType(), task.getStatus(), task.getFileName(),
                task.getRequestedBy(), task.getTotalCount(), task.getProcessedCount(), task.getSuccessCount(),
                task.getFailureCount(), task.getErrorMessage(), task.getStartedTime(), task.getFinishedTime(),
                task.getCreateTime(), task.getUpdateTime());
    }
}
