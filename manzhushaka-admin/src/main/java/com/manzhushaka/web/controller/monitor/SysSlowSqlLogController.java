package com.manzhushaka.web.controller.monitor;

import java.util.List;

import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.system.application.result.system.SlowSqlLogResult;
import com.manzhushaka.system.application.service.SystemAuditAppService;
import com.manzhushaka.web.converter.monitor.AuditAdminConverter;
import com.manzhushaka.web.dto.monitor.SlowSqlLogQueryRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 慢 SQL 日志记录。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@RestController
@RequestMapping("/monitor/slowSql")
public class SysSlowSqlLogController extends BaseController
{
    @Autowired
    private SystemAuditAppService auditAppService;

    @PreAuthorize("@ss.hasPermi('monitor:slowsql:list')")
    @GetMapping("/list")
    public TableDataInfo list(SlowSqlLogQueryRequest request)
    {
        startPage();
        List<SlowSqlLogResult> list = auditAppService.listSlowSqlLogs(AuditAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('monitor:slowsql:query')")
    @GetMapping("/{slowSqlId}")
    public AjaxResult getInfo(@PathVariable Long slowSqlId)
    {
        return success(auditAppService.getSlowSqlLog(slowSqlId));
    }

    @Log(title = "慢 SQL 日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:slowsql:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SlowSqlLogQueryRequest request)
    {
        List<SlowSqlLogResult> list = auditAppService.listSlowSqlLogs(AuditAdminConverter.toQuery(request));
        ExcelUtil<SlowSqlLogResult> util = new ExcelUtil<SlowSqlLogResult>(SlowSqlLogResult.class);
        util.exportExcel(response, list, "慢 SQL 日志");
    }

    @Log(title = "慢 SQL 日志", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('monitor:slowsql:remove')")
    @DeleteMapping("/{slowSqlIds}")
    public AjaxResult remove(@PathVariable Long[] slowSqlIds)
    {
        return toAjax(auditAppService.deleteSlowSqlLogs(slowSqlIds));
    }

    @Log(title = "慢 SQL 日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ss.hasPermi('monitor:slowsql:remove')")
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        auditAppService.cleanSlowSqlLogs();
        return success();
    }
}
