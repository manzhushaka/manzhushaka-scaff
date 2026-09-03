package com.manzhushaka.task.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.context.PermissionContextHolder;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.task.domain.TaskStatus;
import com.manzhushaka.task.infrastructure.persistence.entity.ExportTask;
import com.manzhushaka.task.mapper.ExportTaskMapper;

import tools.jackson.databind.ObjectMapper;

/**
 * 异步导出任务编排服务。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Service
public class ExportTaskManager
{
    public static final String STREAM_KEY = "system:export-task";
    public static final String MESSAGE_TYPE = "SYSTEM_EXPORT_TASK";
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final long LEASE_MILLIS = 10 * 60 * 1000L;

    private final ExportTaskMapper taskMapper;
    private final TaskHandlerRegistry handlerRegistry;
    private final TaskFileStorage fileStorage;
    private final SecuritySnapshotService securitySnapshotService;
    private final RedisStreamMessagePublisher publisher;
    private final ObjectMapper objectMapper;

    public ExportTaskManager(ExportTaskMapper taskMapper, TaskHandlerRegistry handlerRegistry,
            TaskFileStorage fileStorage, SecuritySnapshotService securitySnapshotService,
            RedisStreamMessagePublisher publisher, ObjectMapper objectMapper)
    {
        this.taskMapper = taskMapper;
        this.handlerRegistry = handlerRegistry;
        this.fileStorage = fileStorage;
        this.securitySnapshotService = securitySnapshotService;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    /** 提交异步导出任务。 */
    @Transactional
    public TaskResult submit(String handlerType, Object querySnapshot, String fileName)
    {
        handlerRegistry.get(handlerType);
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        ExportTask task = new ExportTask();
        task.setHandlerType(handlerType);
        task.setStatus(TaskStatus.PENDING.name());
        task.setFileKey(fileStorage.newExportKey());
        task.setFileName(fileName);
        task.setContentType(XLSX_CONTENT_TYPE);
        task.setRequestedBy(principal.getUserId());
        task.setQuerySnapshot(objectMapper.writeValueAsString(querySnapshot));
        task.setSecuritySnapshot(securitySnapshotService.create(principal));
        initializeCounts(task);
        taskMapper.insert(task);
        publishAfterCommit(task.getTaskId());
        return TaskResult.from(task);
    }

    /** 执行一个导出任务。 */
    public void execute(Long taskId)
    {
        Date now = new Date();
        String leaseToken = UUID.randomUUID().toString();
        if (taskMapper.claim(taskId, now, leaseUntil(), leaseToken) == 0)
        {
            return;
        }
        ExportTask task = taskMapper.selectById(taskId);
        if (task == null)
        {
            return;
        }
        org.springframework.security.core.context.SecurityContext previous = null;
        try
        {
            previous = securitySnapshotService.install(task.getSecuritySnapshot());
            Path filePath = fileStorage.resolve(task.getFileKey());
            Files.createDirectories(filePath.getParent());
            TaskContext<ExportTask> context = new TaskContext<>(task, filePath,
                    (total, processed, success, failure) -> taskMapper.updateProgress(taskId, leaseToken,
                            total, processed, success, failure, leaseUntil()), () -> isCancelled(taskId));
            handlerRegistry.get(task.getHandlerType()).execute(context);
            ExportTask latest = taskMapper.selectById(taskId);
            if (latest != null && isCancelStatus(latest.getStatus()))
            {
                if (taskMapper.markCancelled(taskId, leaseToken, new Date()) > 0)
                {
                    fileStorage.deleteQuietly(task.getFileKey());
                }
            }
            else
            {
                String status = latest != null && latest.getFailureCount() != null && latest.getFailureCount() > 0
                        ? TaskStatus.PARTIAL_SUCCESS.name() : TaskStatus.SUCCESS.name();
                taskMapper.markSuccess(taskId, leaseToken, status, new Date());
            }
        }
        catch (TaskCancelledException exception)
        {
            if (taskMapper.markCancelled(taskId, leaseToken, new Date()) > 0)
            {
                fileStorage.deleteQuietly(task.getFileKey());
            }
        }
        catch (Exception exception)
        {
            if (taskMapper.markFailure(taskId, leaseToken, normalizeError(exception), new Date()) > 0)
            {
                fileStorage.deleteQuietly(task.getFileKey());
            }
        }
        finally
        {
            PermissionContextHolder.clearContext();
            securitySnapshotService.restore(previous);
        }
    }

    /** 按提交人查询任务。管理员传 null 查询全部。 */
    public List<ExportTask> list(Long requestedBy, String status)
    {
        return taskMapper.selectList(requestedBy, status);
    }

    /** 查询有权访问的任务。 */
    public ExportTask authorized(Long taskId, Long requestedBy, boolean administrator)
    {
        ExportTask task = taskMapper.selectById(taskId);
        if (task == null)
        {
            throw new ServiceException("导出任务不存在");
        }
        if (!administrator && !java.util.Objects.equals(requestedBy, task.getRequestedBy()))
        {
            throw new org.springframework.security.access.AccessDeniedException("无权访问该导出任务");
        }
        return task;
    }

    /** 请求取消导出任务。 */
    @Transactional
    public void requestCancel(Long taskId)
    {
        ExportTask task = taskMapper.selectById(taskId);
        if (taskMapper.requestCancel(taskId) == 0)
        {
            throw new ServiceException("导出任务当前状态不可取消");
        }
        if (task != null && TaskStatus.PENDING.name().equals(task.getStatus()))
        {
            fileStorage.deleteQuietly(task.getFileKey());
        }
    }

    /** 获取可下载文件。 */
    public TaskDownloadResult resolveDownload(Long taskId, Long requestedBy, boolean administrator)
    {
        ExportTask task = authorized(taskId, requestedBy, administrator);
        if (!TaskStatus.SUCCESS.name().equals(task.getStatus())
                && !TaskStatus.PARTIAL_SUCCESS.name().equals(task.getStatus()))
        {
            throw new ServiceException("任务尚未生成可下载文件");
        }
        Path path = fileStorage.resolve(task.getFileKey());
        if (!Files.isRegularFile(path))
        {
            throw new ServiceException("导出文件不存在或已清理");
        }
        return new TaskDownloadResult(path, task.getFileName(), task.getContentType());
    }

    /** 重新投递待执行和租约过期任务。 */
    @Scheduled(fixedDelay = 30000L)
    public void dispatchRecoverableTasks()
    {
        for (ExportTask task : taskMapper.selectRecoverable(new Date()))
        {
            publish(task.getTaskId());
        }
    }

    private boolean isCancelled(Long taskId)
    {
        ExportTask task = taskMapper.selectById(taskId);
        return task != null && isCancelStatus(task.getStatus());
    }

    private boolean isCancelStatus(String status)
    {
        return TaskStatus.CANCEL_REQUESTED.name().equals(status) || TaskStatus.CANCELLED.name().equals(status);
    }

    private Date leaseUntil()
    {
        return new Date(System.currentTimeMillis() + LEASE_MILLIS);
    }

    private void initializeCounts(ExportTask task)
    {
        task.setTotalCount(0L);
        task.setProcessedCount(0L);
        task.setSuccessCount(0L);
        task.setFailureCount(0L);
    }

    private String normalizeError(Exception exception)
    {
        String message = exception.getMessage();
        if (message == null || message.isBlank())
        {
            return "导出任务执行失败";
        }
        return message.length() > 1800 ? message.substring(0, 1800) : message;
    }

    private void publish(Long taskId)
    {
        publisher.publish(STREAM_KEY, MESSAGE_TYPE, String.valueOf(taskId), String.valueOf(taskId));
    }

    private void publishAfterCommit(Long taskId)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            publish(taskId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                publish(taskId);
            }
        });
    }
}
