package com.manzhushaka.web.controller.monitor;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.system.application.result.system.MqMessageLogResult;
import com.manzhushaka.system.application.service.SystemAuditAppService;
import com.manzhushaka.web.converter.monitor.AuditAdminConverter;
import com.manzhushaka.web.dto.monitor.MqMessageLogQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息队列台账 Controller。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@RestController
@RequestMapping("/monitor/mqLog")
public class SysMqMessageLogController extends BaseController
{
    @Autowired
    private SystemAuditAppService auditAppService;

    /**
     * 查询消息队列台账列表。
     *
     * @param messageLog 查询条件
     * @return 分页列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:mqlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(MqMessageLogQueryRequest request)
    {
        startPage();
        List<MqMessageLogResult> list = auditAppService.listMqMessageLogs(AuditAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    /**
     * 查询消息队列台账详情。
     *
     * @param messageLogId 消息台账主键
     * @return 详情
     */
    @PreAuthorize("@ss.hasPermi('monitor:mqlog:query')")
    @GetMapping("/{messageLogId}")
    public AjaxResult getInfo(@PathVariable Long messageLogId)
    {
        return success(auditAppService.getMqMessageLog(messageLogId));
    }

    /**
     * 查询消息队列执行明细。
     *
     * @param messageLogId 消息台账主键
     * @return 明细列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:mqlog:query')")
    @GetMapping("/{messageLogId}/details")
    public AjaxResult detailList(@PathVariable Long messageLogId)
    {
        return success(auditAppService.listMqMessageLogDetails(messageLogId));
    }

    /**
     * 导出消息队列台账。
     *
     * @param response   HTTP 响应
     * @param messageLog 查询条件
     */
    @Log(title = "消息队列台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:mqlog:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MqMessageLogQueryRequest request)
    {
        List<MqMessageLogResult> list = auditAppService.listMqMessageLogs(AuditAdminConverter.toQuery(request));
        ExcelUtil<MqMessageLogResult> util = new ExcelUtil<>(MqMessageLogResult.class);
        util.exportExcel(response, list, "消息队列台账");
    }

    /**
     * 删除消息队列台账。
     *
     * @param messageLogIds 消息台账主键数组
     * @return 操作结果
     */
    @Log(title = "消息队列台账", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('monitor:mqlog:remove')")
    @DeleteMapping("/{messageLogIds}")
    public AjaxResult remove(@PathVariable Long[] messageLogIds)
    {
        return toAjax(auditAppService.deleteMqMessageLogs(messageLogIds));
    }

    /**
     * 清空消息队列台账。
     *
     * @return 操作结果
     */
    @Log(title = "消息队列台账", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ss.hasPermi('monitor:mqlog:remove')")
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        auditAppService.cleanMqMessageLogs();
        return success();
    }
}
