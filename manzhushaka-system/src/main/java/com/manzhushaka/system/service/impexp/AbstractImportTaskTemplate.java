package com.manzhushaka.system.service.impexp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import org.springframework.util.StringUtils;

/**
 * 业务模块通过继承该模板定义导入场景，再由各自的业务入口提交任务。
 */
public abstract class AbstractImportTaskTemplate<C extends ImportTaskSubmitCommand> extends BaseImportExportTaskTemplate {
    private static final long MAX_IMPORT_FILE_SIZE = 50L * 1024 * 1024;

    private final Class<C> commandType;

    protected AbstractImportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        ObjectMapper objectMapper,
        Class<C> commandType
    ) {
        super(taskMapper, storageService, objectMapper);
        this.commandType = commandType;
    }

    protected AbstractImportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        ObjectMapper objectMapper,
        Class<C> commandType,
        String storageBasePath
    ) {
        super(taskMapper, storageService, objectMapper, storageBasePath);
        this.commandType = commandType;
    }

    /**
     * 创建 submit 数据。
     *
     * @param command command 参数
     * @param operator operator 参数
     * @return 创建结果
     */
    public final Long submit(C command, LoginUser operator) {
        if (command == null) {
            throw new BizException(400, "导入任务参数不能为空");
        }
        validateSubmit(command);
        String taskNo = ImportExportTaskSupport.nextTaskNo("IMP");
        String sourceObjectKey = buildSourceObjectKey(taskNo, command.getFileName());
        byte[] content = command.getContent();
        storageService.putObject(sourceObjectKey, content, command.getContentType());

        SysImportExportTask task = new SysImportExportTask();
        task.setTaskNo(taskNo);
        task.setTaskType(ImportExportTaskSupport.TASK_TYPE_IMPORT);
        task.setBizType(bizType());
        task.setBizLabel(bizLabel());
        task.setTaskName(resolveTaskName(command));
        task.setTaskStatus(ImportExportTaskSupport.TASK_STATUS_PENDING);
        task.setTaskMessage("任务待执行");
        task.setTaskParam(writeTaskParam(command));
        task.setSourceFileName(command.getFileName());
        task.setSourceObjectKey(sourceObjectKey);
        task.setSourceFileSize((long) content.length);
        applyOperator(task, operator);
        taskMapper.insert(task);
        return task.getId();
    }

    /**
     * 执行任务处理。
     *
     * @param taskId 任务 ID
     */
    public final void execute(Long taskId) {
        SysImportExportTask task = loadTask(taskId);
        if (task == null) {
            return;
        }
        try {
            markProcessing(task);
            byte[] sourceContent = storageService.getObjectContent(task.getSourceObjectKey());
            TaskSourceFile sourceFile = new TaskSourceFile(task.getSourceFileName(), sourceContent);
            markSuccess(task, executeImport(task, readTaskParam(task.getTaskParam(), commandType), sourceFile));
        } catch (Exception exception) {
            markFail(task, exception);
        }
    }

    /**
     * 构建 resolve Task Name 结果。
     *
     * @param command command 参数
     * @return 处理结果
     */
    protected String resolveTaskName(C command) {
        if (StringUtils.hasText(command.getTaskName())) {
            return command.getTaskName().trim();
        }
        return defaultTaskName();
    }

    /**
     * 校验导入任务提交参数的基础约束。
     *
     * @param command 导入任务提交参数
     */
    protected void validateSubmit(C command) {
        if (!StringUtils.hasText(command.getFileName()) || !isSupportedImportFile(command.getFileName())) {
            throw new BizException(400, "仅支持上传 csv、xls、xlsx 格式文件");
        }
        if (command.getContent().length > MAX_IMPORT_FILE_SIZE) {
            throw new BizException(400, "导入文件大小不能超过 50MB");
        }
    }

    /**
     * 判断是否为允许的导入文件格式。
     *
     * @param fileName 文件名
     * @return true 表示允许上传
     */
    private boolean isSupportedImportFile(String fileName) {
        String normalized = fileName.trim().toLowerCase();
        return normalized.endsWith(".csv") || normalized.endsWith(".xls") || normalized.endsWith(".xlsx");
    }

    protected abstract TaskExecutionResult executeImport(SysImportExportTask task, C command, TaskSourceFile sourceFile)
        throws Exception;
}
