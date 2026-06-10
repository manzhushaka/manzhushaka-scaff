package com.manzhushaka.system.service.impexp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ImportExportTaskAsyncExecutor {

    private final ExportTaskTemplateRegistry exportTaskTemplateRegistry;
    private final ImportTaskTemplateRegistry importTaskTemplateRegistry;

    public ImportExportTaskAsyncExecutor(
        ExportTaskTemplateRegistry exportTaskTemplateRegistry,
        ImportTaskTemplateRegistry importTaskTemplateRegistry
    ) {
        this.exportTaskTemplateRegistry = exportTaskTemplateRegistry;
        this.importTaskTemplateRegistry = importTaskTemplateRegistry;
    }

    @Async
    public void dispatch(String taskType, String bizType, Long taskId) {
        if (ImportExportTaskSupport.TASK_TYPE_EXPORT.equals(taskType)) {
            exportTaskTemplateRegistry.getRequired(bizType).execute(taskId);
            return;
        }
        if (ImportExportTaskSupport.TASK_TYPE_IMPORT.equals(taskType)) {
            importTaskTemplateRegistry.getRequired(bizType).execute(taskId);
        }
    }
}
