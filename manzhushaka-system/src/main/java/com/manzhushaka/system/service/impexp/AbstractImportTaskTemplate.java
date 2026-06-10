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

    protected String resolveTaskName(C command) {
        if (StringUtils.hasText(command.getTaskName())) {
            return command.getTaskName().trim();
        }
        return defaultTaskName();
    }

    protected void validateSubmit(C command) {
    }

    protected abstract TaskExecutionResult executeImport(SysImportExportTask task, C command, TaskSourceFile sourceFile)
        throws Exception;
}
