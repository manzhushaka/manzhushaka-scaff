package com.manzhushaka.system.service.impexp;

import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import com.manzhushaka.system.dto.impexp.ImportTaskCreateCommand;
import org.springframework.util.StringUtils;

public abstract class AbstractImportTaskTemplate extends BaseImportExportTaskTemplate {

    protected AbstractImportTaskTemplate(SysImportExportTaskMapper taskMapper, ObjectStorageService storageService) {
        super(taskMapper, storageService);
    }

    protected AbstractImportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        String storageBasePath
    ) {
        super(taskMapper, storageService, storageBasePath);
    }

    public final Long submit(ImportTaskCreateCommand command, LoginUser operator) {
        ensureBizType(command.getBizType());
        validateSubmit(command);
        String taskNo = ImportExportTaskSupport.nextTaskNo("IMP");
        String sourceObjectKey = buildSourceObjectKey(taskNo, command.getFileName());
        storageService.putObject(sourceObjectKey, command.getContent(), command.getContentType());

        SysImportExportTask task = new SysImportExportTask();
        task.setTaskNo(taskNo);
        task.setTaskType(ImportExportTaskSupport.TASK_TYPE_IMPORT);
        task.setBizType(bizType());
        task.setBizLabel(bizLabel());
        task.setTaskName(StringUtils.hasText(command.getTaskName()) ? command.getTaskName().trim() : defaultTaskName());
        task.setTaskStatus(ImportExportTaskSupport.TASK_STATUS_PENDING);
        task.setTaskMessage("任务待执行");
        task.setSourceFileName(command.getFileName());
        task.setSourceObjectKey(sourceObjectKey);
        task.setSourceFileSize((long) command.getContent().length);
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
            markSuccess(task, executeImport(task, sourceFile));
        } catch (Exception exception) {
            markFail(task, exception);
        }
    }

    protected void validateSubmit(ImportTaskCreateCommand command) {
    }

    protected abstract TaskExecutionResult executeImport(SysImportExportTask task, TaskSourceFile sourceFile) throws Exception;
}
