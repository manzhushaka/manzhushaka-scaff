package com.manzhushaka.web.controller.monitor;

import java.util.List;

import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.system.domain.SysRequestLog;
import com.manzhushaka.system.service.ISysRequestLogService;
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
 * 请求日志记录。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@RestController
@RequestMapping("/monitor/requestLog")
public class SysRequestLogController extends BaseController
{
    @Autowired
    private ISysRequestLogService requestLogService;

    @PreAuthorize("@ss.hasPermi('monitor:requestlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysRequestLog requestLog)
    {
        startPage();
        List<SysRequestLog> list = requestLogService.selectRequestLogList(requestLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('monitor:requestlog:query')")
    @GetMapping("/{requestId}")
    public AjaxResult getInfo(@PathVariable Long requestId)
    {
        return success(requestLogService.selectRequestLogById(requestId));
    }

    @Log(title = "请求日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:requestlog:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysRequestLog requestLog)
    {
        List<SysRequestLog> list = requestLogService.selectRequestLogList(requestLog);
        ExcelUtil<SysRequestLog> util = new ExcelUtil<SysRequestLog>(SysRequestLog.class);
        util.exportExcel(response, list, "请求日志");
    }

    @Log(title = "请求日志", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('monitor:requestlog:remove')")
    @DeleteMapping("/{requestIds}")
    public AjaxResult remove(@PathVariable Long[] requestIds)
    {
        return toAjax(requestLogService.deleteRequestLogByIds(requestIds));
    }

    @Log(title = "请求日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ss.hasPermi('monitor:requestlog:remove')")
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        requestLogService.cleanRequestLog();
        return success();
    }
}
