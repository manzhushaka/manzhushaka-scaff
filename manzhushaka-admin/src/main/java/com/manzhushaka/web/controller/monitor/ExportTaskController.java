package com.manzhushaka.web.controller.monitor;

import java.nio.file.Files;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.file.FileUtils;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.task.application.ExportTaskManager;
import com.manzhushaka.task.application.SystemUserExportTaskHandler;
import com.manzhushaka.task.application.TaskDownloadResult;
import com.manzhushaka.task.application.TaskResult;
import com.manzhushaka.web.converter.system.user.UserAdminConverter;
import com.manzhushaka.web.dto.monitor.TaskListRequest;
import com.manzhushaka.web.dto.system.user.UserListRequest;

/**
 * 异步导出任务接口。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@RestController
@RequestMapping("/monitor/exportTask")
public class ExportTaskController extends BaseController
{
    private final ExportTaskManager taskManager;

    public ExportTaskController(ExportTaskManager taskManager)
    {
        this.taskManager = taskManager;
    }

    /** 查询导出任务列表。 */
    @PreAuthorize("@ss.hasPermi('monitor:exporttask:list')")
    @GetMapping("/list")
    public TableDataInfo list(TaskListRequest request)
    {
        startPage();
        Long requestedBy = SecurityContextHelper.isAdmin() ? null : SecurityContextHelper.getUserId();
        List<TaskResult> tasks = taskManager.list(requestedBy, request.getStatus())
                .stream().map(TaskResult::from).toList();
        return getDataTable(tasks);
    }

    /** 查询导出任务详情。 */
    @PreAuthorize("@ss.hasPermi('monitor:exporttask:query')")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return success(TaskResult.from(taskManager.authorized(taskId,
                SecurityContextHelper.getUserId(), SecurityContextHelper.isAdmin())));
    }

    /** 提交系统用户导出任务。 */
    @Log(title = "异步导出任务", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:exporttask:submit')")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody UserListRequest request)
    {
        return success(taskManager.submit(SystemUserExportTaskHandler.HANDLER_TYPE,
                UserAdminConverter.toUserListQuery(request), "用户数据.xlsx"));
    }

    /** 取消导出任务。 */
    @Log(title = "异步导出任务", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('monitor:exporttask:cancel')")
    @DeleteMapping("/{taskId}")
    public AjaxResult cancel(@PathVariable Long taskId)
    {
        taskManager.authorized(taskId, SecurityContextHelper.getUserId(), SecurityContextHelper.isAdmin());
        taskManager.requestCancel(taskId);
        return success();
    }

    /** 下载导出任务文件。 */
    @PreAuthorize("@ss.hasPermi('monitor:exporttask:download')")
    @GetMapping("/{taskId}/download")
    public void download(@PathVariable Long taskId, HttpServletResponse response) throws Exception
    {
        TaskDownloadResult result = taskManager.resolveDownload(taskId,
                SecurityContextHelper.getUserId(), SecurityContextHelper.isAdmin());
        response.setContentType(result.contentType());
        FileUtils.setAttachmentResponseHeader(response, result.fileName());
        Files.copy(result.path(), response.getOutputStream());
    }
}
