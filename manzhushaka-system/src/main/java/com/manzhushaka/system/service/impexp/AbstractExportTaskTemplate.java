package com.manzhushaka.system.service.impexp;

import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import com.manzhushaka.system.dto.impexp.ExportTaskCreateForm;
import org.springframework.util.StringUtils;

public abstract class AbstractExportTaskTemplate extends BaseImportExportTaskTemplate {

    protected AbstractExportTaskTemplate(SysImportExportTaskMapper taskMapper, ObjectStorageService storageService) {
        super(taskMapper, storageService);
    }

    protected AbstractExportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        String storageBasePath
    ) {
        super(taskMapper, storageService, storageBasePath);
    }

    public final Long submit(ExportTaskCreateForm form, LoginUser operator) {
        ensureBizType(form.getBizType());
        validateSubmit(form);
        SysImportExportTask task = new SysImportExportTask();
        task.setTaskNo(ImportExportTaskSupport.nextTaskNo("EXP"));
        task.setTaskType(ImportExportTaskSupport.TASK_TYPE_EXPORT);
        task.setBizType(bizType());
        task.setBizLabel(bizLabel());
        task.setTaskName(StringUtils.hasText(form.getTaskName()) ? form.getTaskName().trim() : defaultTaskName());
        task.setTaskStatus(ImportExportTaskSupport.TASK_STATUS_PENDING);
        task.setTaskMessage("任务待执行");
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
            markSuccess(task, executeExport(task));
        } catch (Exception exception) {
            markFail(task, exception);
        }
    }

    protected void validateSubmit(ExportTaskCreateForm form) {
    }

    protected abstract TaskExecutionResult executeExport(SysImportExportTask task) throws Exception;
}
