package com.manzhushaka.system.service.impexp;

import com.manzhushaka.common.context.LoginUser;
import org.springframework.stereotype.Component;

/**
 * 定义 ImportExportTaskManager。
 */
@Component
public class ImportExportTaskManager {

    private final ImportExportTaskAsyncExecutor asyncExecutor;

    /**
     * 创建 ImportExportTaskManager 实例。
     *
     * @param asyncExecutor asyncExecutor 参数
     */
    public ImportExportTaskManager(ImportExportTaskAsyncExecutor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    public <C extends ExportTaskSubmitCommand> Long submitExportTask(
        AbstractExportTaskTemplate<C> template,
        C command,
        LoginUser operator
    ) {
        Long taskId = template.submit(command, operator);
        asyncExecutor.dispatch(ImportExportTaskSupport.TASK_TYPE_EXPORT, template.bizType(), taskId);
        return taskId;
    }

    public <C extends ImportTaskSubmitCommand> Long submitImportTask(
        AbstractImportTaskTemplate<C> template,
        C command,
        LoginUser operator
    ) {
        Long taskId = template.submit(command, operator);
        asyncExecutor.dispatch(ImportExportTaskSupport.TASK_TYPE_IMPORT, template.bizType(), taskId);
        return taskId;
    }
}
