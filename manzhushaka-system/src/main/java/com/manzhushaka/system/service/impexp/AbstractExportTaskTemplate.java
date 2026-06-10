package com.manzhushaka.system.service.impexp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import org.springframework.util.StringUtils;

/**
 * 业务模块通过继承该模板定义导出场景，再由各自的业务入口提交任务。
 */
public abstract class AbstractExportTaskTemplate<C extends ExportTaskSubmitCommand> extends BaseImportExportTaskTemplate {

    private final Class<C> commandType;

    protected AbstractExportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        ObjectMapper objectMapper,
        Class<C> commandType
    ) {
        super(taskMapper, storageService, objectMapper);
        this.commandType = commandType;
    }

    protected AbstractExportTaskTemplate(
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
            throw new BizException(400, "导出任务参数不能为空");
        }
        validateSubmit(command);
        SysImportExportTask task = new SysImportExportTask();
        task.setTaskNo(ImportExportTaskSupport.nextTaskNo("EXP"));
        task.setTaskType(ImportExportTaskSupport.TASK_TYPE_EXPORT);
        task.setBizType(bizType());
        task.setBizLabel(bizLabel());
        task.setTaskName(resolveTaskName(command));
        task.setTaskStatus(ImportExportTaskSupport.TASK_STATUS_PENDING);
        task.setTaskMessage("任务待执行");
        task.setTaskParam(writeTaskParam(command));
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
            markSuccess(task, executeExport(task, readTaskParam(task.getTaskParam(), commandType)));
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

    protected abstract TaskExecutionResult executeExport(SysImportExportTask task, C command) throws Exception;
}
