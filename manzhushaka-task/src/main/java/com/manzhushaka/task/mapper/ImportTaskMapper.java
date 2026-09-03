package com.manzhushaka.task.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.manzhushaka.task.infrastructure.persistence.entity.ImportTask;

/**
 * 异步导入任务数据层。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public interface ImportTaskMapper
{
    int insert(ImportTask task);
    ImportTask selectById(Long taskId);
    List<ImportTask> selectList(@Param("requestedBy") Long requestedBy, @Param("status") String status);
    int claim(@Param("taskId") Long taskId, @Param("startedTime") Date startedTime,
            @Param("leaseUntil") Date leaseUntil, @Param("leaseToken") String leaseToken);
    int updateProgress(@Param("taskId") Long taskId, @Param("leaseToken") String leaseToken,
            @Param("total") long total, @Param("processed") long processed, @Param("success") long success,
            @Param("failure") long failure, @Param("leaseUntil") Date leaseUntil);
    int markSuccess(@Param("taskId") Long taskId, @Param("leaseToken") String leaseToken,
            @Param("status") String status, @Param("finishedTime") Date finishedTime);
    int markFailure(@Param("taskId") Long taskId, @Param("leaseToken") String leaseToken,
            @Param("message") String message, @Param("finishedTime") Date finishedTime);
    int markCancelled(@Param("taskId") Long taskId, @Param("leaseToken") String leaseToken,
            @Param("finishedTime") Date finishedTime);
    int requestCancel(Long taskId);
    List<ImportTask> selectRecoverable(@Param("now") Date now);
}
