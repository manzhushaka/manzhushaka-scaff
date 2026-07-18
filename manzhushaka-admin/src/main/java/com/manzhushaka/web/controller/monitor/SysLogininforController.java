package com.manzhushaka.web.controller.monitor;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.web.service.SysPasswordService;
import com.manzhushaka.system.application.result.system.LoginLogResult;
import com.manzhushaka.system.application.service.SystemAuditAppService;
import com.manzhushaka.web.converter.monitor.AuditAdminConverter;
import com.manzhushaka.web.dto.monitor.LoginLogQueryRequest;

/**
 * 系统访问记录
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController
{
    @Autowired
    private SystemAuditAppService auditAppService;

    @Autowired
    private SysPasswordService passwordService;

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:list')")
    @GetMapping("/list")
    public TableDataInfo list(LoginLogQueryRequest request)
    {
        startPage();
        List<LoginLogResult> list = auditAppService.listLoginLogs(AuditAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:logininfor:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, LoginLogQueryRequest request)
    {
        List<LoginLogResult> list = auditAppService.listLoginLogs(AuditAdminConverter.toQuery(request));
        ExcelUtil<LoginLogResult> util = new ExcelUtil<LoginLogResult>(LoginLogResult.class);
        util.exportExcel(response, list, "登录日志");
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(auditAppService.deleteLoginLogs(infoIds));
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        auditAppService.cleanLoginLogs();
        return success();
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:unlock')")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName)
    {
        passwordService.clearLoginRecordCache(userName);
        return success();
    }
}
