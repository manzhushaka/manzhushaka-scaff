package com.manzhushaka.task.application;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.springframework.stereotype.Component;

import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.security.context.PermissionContextHolder;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.system.application.result.system.UserExcelRow;
import com.manzhushaka.system.application.result.system.UserImportBatchResult;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.task.infrastructure.persistence.entity.ImportTask;

/**
 * 系统用户异步导入处理器。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Component
public class SystemUserImportTaskHandler extends AbstractImportTaskHandler<UserExcelRow>
{
    public static final String HANDLER_TYPE = "SYSTEM_USER_IMPORT";
    private static final String DATA_SCOPE_PERMISSION = "monitor:importtask:submit";

    private final SystemUserAppService userAppService;

    public SystemUserImportTaskHandler(SystemUserAppService userAppService)
    {
        this.userAppService = userAppService;
    }

    @Override
    public String handlerType()
    {
        return HANDLER_TYPE;
    }

    @Override
    protected List<UserExcelRow> readRows(TaskContext<?> context) throws Exception
    {
        ExcelUtil<UserExcelRow> util = new ExcelUtil<>(UserExcelRow.class);
        try (InputStream inputStream = Files.newInputStream(context.filePath()))
        {
            return util.importExcel(inputStream);
        }
    }

    @Override
    protected ImportBatchResult processBatch(TaskContext<?> context, List<UserExcelRow> batch)
    {
        checkCancelled(context);
        ImportTask task = (ImportTask) context.task();
        PermissionContextHolder.setContext(DATA_SCOPE_PERMISSION);
        UserImportBatchResult result = userAppService.importUserRowsBatch(batch,
                Boolean.TRUE.equals(task.getUpdateSupport()), SecurityContextHelper.getUsername());
        return new ImportBatchResult(result.successCount(), result.failureCount());
    }
}
