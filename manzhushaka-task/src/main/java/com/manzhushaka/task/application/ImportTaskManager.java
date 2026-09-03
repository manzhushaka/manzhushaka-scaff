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
import org.springframework.web.multipart.MultipartFile;

import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.context.PermissionContextHolder;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.task.domain.TaskStatus;
import com.manzhushaka.task.infrastructure.persistence.entity.ImportTask;
import com.manzhushaka.task.mapper.ImportTaskMapper;

import tools.jackson.databind.ObjectMapper;

/**
 * 异步导入任务编排服务。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Service
public class ImportTaskManager
{
    public static final String STREAM_KEY = "system:import-task";
    public static final String MESSAGE_TYPE = "SYSTEM_IMPORT_TASK";
    private static final long LEASE_MILLIS = 10 * 60 * 1000L;

    private final ImportTaskMapper taskMapper;
    private final TaskHandlerRegistry handlerRegistry;
    private final TaskFileStorage fileStorage;
    private final SecuritySnapshotService securitySnapshotService;
    private final RedisStreamMessagePublisher publisher;
    private final ObjectMapper objectMapper;

    public ImportTaskManager(ImportTaskMapper taskMapper, TaskHandlerRegistry handlerRegistry,
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

    /** 保存上传文件并提交异步导入任务。 */
    @Transactional
    public TaskResult submit(String handlerType, MultipartFile file, boolean updateSupport) throws Exception
    {
        handlerRegistry.get(handlerType);
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        String fileKey = fileStorage.storeImport(file);
        try
        {
            ImportTask task = new ImportTask();
            task.setHandlerType(handlerType);
            task.setStatus(TaskStatus.PENDING.name());
            task.setFileKey(fileKey);
            task.setFileName(file.getOriginalFilename());
            task.setContentType(file.getContentType());
            task.setRequestedBy(principal.getUserId());
            task.setUpdateSupport(updateSupport);
            task.setOptionsSnapshot(objectMapper.writeValueAsString(java.util.Map.of("updateSupport", updateSupport)));
            task.setSecuritySnapshot(securitySnapshotService.create(principal));
            initializeCounts(task);
            taskMapper.insert(task);
            publishAfterCommit(task.getTaskId());
            return TaskResult.from(task);
        }
        catch (Exception exception)
        {
            fileStorage.deleteQuietly(fileKey);
            throw exception;
        }
    }

    /** 执行一个导入任务。 */
    public void execute(Long taskId)
    {
        Date now = new Date();
        String leaseToken = UUID.randomUUID().toString();
        Date leaseUntil = new Date(now.getTime() + LEASE_MILLIS);
        if (taskMapper.claim(taskId, now, leaseUntil, leaseToken) == 0)
        {
            return;
        }
        ImportTask task = taskMapper.selectById(taskId);
        if (task == null)
        {
            return;
        }
        org.springframework.security.core.context.SecurityContext previous = null;
        boolean terminalStateOwned = false;
        try
        {
            previous = securitySnapshotService.install(task.getSecuritySnapshot());
            Path filePath = fileStorage.resolve(task.getFileKey());
            Files.createDirectories(filePath.getParent());
            TaskContext<ImportTask> context = new TaskContext<>(task, filePath,
                    (total, processed, success, failure) -> taskMapper.updateProgress(taskId, leaseToken,
                            total, processed, success, failure, leaseUntil()), () -> isCancelled(taskId));
            handlerRegistry.get(task.getHandlerType()).execute(context);
            ImportTask latest = taskMapper.selectById(taskId);
            if (latest != null && isCancelStatus(latest.getStatus()))
            {
                terminalStateOwned = taskMapper.markCancelled(taskId, leaseToken, new Date()) > 0;
            }
            else
            {
                String status = latest != null && latest.getFailureCount() != null && latest.getFailureCount() > 0
                        ? TaskStatus.PARTIAL_SUCCESS.name() : TaskStatus.SUCCESS.name();
                terminalStateOwned = taskMapper.markSuccess(taskId, leaseToken, status, new Date()) > 0;
            }
        }
        catch (TaskCancelledException exception)
        {
            terminalStateOwned = taskMapper.markCancelled(taskId, leaseToken, new Date()) > 0;
        }
        catch (Exception exception)
        {
            terminalStateOwned = taskMapper.markFailure(taskId, leaseToken, normalizeError(exception), new Date()) > 0;
        }
        finally
        {
            if (terminalStateOwned)
            {
                fileStorage.deleteQuietly(task.getFileKey());
            }
            PermissionContextHolder.clearContext();
            securitySnapshotService.restore(previous);
        }
    }

    /** 按提交人查询任务。管理员传 null 查询全部。 */
    public List<ImportTask> list(Long requestedBy, String status)
    {
        return taskMapper.selectList(requestedBy, status);
    }

    /** 查询有权访问的任务。 */
    public ImportTask authorized(Long taskId, Long requestedBy, boolean administrator)
    {
        ImportTask task = taskMapper.selectById(taskId);
        if (task == null)
        {
            throw new ServiceException("导入任务不存在");
        }
        if (!administrator && !java.util.Objects.equals(requestedBy, task.getRequestedBy()))
        {
            throw new org.springframework.security.access.AccessDeniedException("无权访问该导入任务");
        }
        return task;
    }

    /** 请求取消导入任务。 */
    @Transactional
    public void requestCancel(Long taskId)
    {
        ImportTask task = taskMapper.selectById(taskId);
        if (taskMapper.requestCancel(taskId) == 0)
        {
            throw new ServiceException("导入任务当前状态不可取消");
        }
        if (task != null && TaskStatus.PENDING.name().equals(task.getStatus()))
        {
            fileStorage.deleteQuietly(task.getFileKey());
        }
    }

    /** 重新投递待执行和租约过期任务。 */
    @Scheduled(fixedDelay = 30000L)
    public void dispatchRecoverableTasks()
    {
        for (ImportTask task : taskMapper.selectRecoverable(new Date()))
        {
            publish(task.getTaskId());
        }
    }

    private boolean isCancelled(Long taskId)
    {
        ImportTask task = taskMapper.selectById(taskId);
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

    private void initializeCounts(ImportTask task)
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
            return "导入任务执行失败";
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
