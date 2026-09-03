package com.manzhushaka.web.controller.monitor;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.task.application.ImportTaskManager;
import com.manzhushaka.task.application.SystemUserImportTaskHandler;
import com.manzhushaka.task.application.TaskResult;
import com.manzhushaka.web.dto.monitor.TaskListRequest;

/**
 * 异步导入任务接口。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@RestController
@RequestMapping("/monitor/importTask")
public class ImportTaskController extends BaseController
{
    private final ImportTaskManager taskManager;

    public ImportTaskController(ImportTaskManager taskManager)
    {
        this.taskManager = taskManager;
    }

    /** 查询导入任务列表。 */
    @PreAuthorize("@ss.hasPermi('monitor:importtask:list')")
    @GetMapping("/list")
    public TableDataInfo list(TaskListRequest request)
    {
        startPage();
        Long requestedBy = SecurityContextHelper.isAdmin() ? null : SecurityContextHelper.getUserId();
        List<TaskResult> tasks = taskManager.list(requestedBy, request.getStatus())
                .stream().map(TaskResult::from).toList();
        return getDataTable(tasks);
    }

    /** 查询导入任务详情。 */
    @PreAuthorize("@ss.hasPermi('monitor:importtask:query')")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return success(TaskResult.from(taskManager.authorized(taskId,
                SecurityContextHelper.getUserId(), SecurityContextHelper.isAdmin())));
    }

    /** 提交系统用户导入任务。 */
    @Log(title = "异步导入任务", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('monitor:importtask:submit')")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean updateSupport) throws Exception
    {
        return success(taskManager.submit(SystemUserImportTaskHandler.HANDLER_TYPE, file, updateSupport));
    }

    /** 取消导入任务。 */
    @Log(title = "异步导入任务", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('monitor:importtask:cancel')")
    @DeleteMapping("/{taskId}")
    public AjaxResult cancel(@PathVariable Long taskId)
    {
        taskManager.authorized(taskId, SecurityContextHelper.getUserId(), SecurityContextHelper.isAdmin());
        taskManager.requestCancel(taskId);
        return success();
    }
}
