package com.manzhushaka.web.controller.iip;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.invoice.result.InvoiceResult;
import com.manzhushaka.iip.application.invoice.service.InvoiceAppService;
import com.manzhushaka.web.converter.iip.InvoiceAdminConverter;
import com.manzhushaka.web.dto.iip.InvoiceAuditRequest;
import com.manzhushaka.web.dto.iip.InvoiceRequest;

/**
 * 发票审核 信息操作处理
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/iip/invoice")
public class IipInvoiceController extends BaseController
{
    @Autowired
    private InvoiceAppService invoiceAppService;

    /**
     * 获取发票列表
     */
    @PreAuthorize("@ss.hasPermi('iip:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvoiceRequest request)
    {
        startPage();
        List<InvoiceResult> list = invoiceAppService.listInvoices(InvoiceAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    /**
     * 导出发票列表
     */
    @Log(title = "发票审核", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('iip:invoice:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, InvoiceRequest request)
    {
        List<InvoiceResult> list = invoiceAppService.listInvoices(InvoiceAdminConverter.toQuery(request));
        ExcelUtil<InvoiceResult> util = new ExcelUtil<InvoiceResult>(InvoiceResult.class);
        util.exportExcel(response, list, "发票数据");
    }

    /**
     * 根据发票编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('iip:invoice:query')")
    @GetMapping(value = "/getInfo/{invoiceId}")
    public AjaxResult getInfo(@PathVariable Long invoiceId)
    {
        return success(invoiceAppService.getInvoice(invoiceId));
    }

    /**
     * 审核发票（通过或驳回）
     */
    @Log(title = "发票审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('iip:invoice:audit')")
    @PutMapping("/audit")
    public AjaxResult audit(@Validated @RequestBody InvoiceAuditRequest request)
    {
        invoiceAppService.auditInvoice(InvoiceAdminConverter.toAuditCommand(request),
                SecurityContextHelper.getUsername());
        return success();
    }
}
