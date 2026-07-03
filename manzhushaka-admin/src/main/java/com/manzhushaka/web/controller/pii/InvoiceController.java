package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.query.InvoicePageQuery;
import com.manzhushaka.biz.pii.application.service.InvoiceQueryService;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.web.dto.pii.InvoicePageRequest;
import com.manzhushaka.web.vo.pii.InvoiceVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pii/invoice")
public class InvoiceController extends BaseController {
    private final InvoiceQueryService invoiceQueryService;

    public InvoiceController(InvoiceQueryService invoiceQueryService) {
        this.invoiceQueryService = invoiceQueryService;
    }

    @PreAuthorize("@ss.hasPermi('biz:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvoicePageRequest request) {
        startPage();
        List<InvoiceVO> rows = listRows(request);
        return getDataTable(rows);
    }

    @Log(title = "发票查询", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('biz:invoice:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvoicePageRequest request) {
        ExcelUtil<InvoiceVO> util = new ExcelUtil<>(InvoiceVO.class);
        util.exportExcel(response, listRows(request), "发票数据");
    }

    @PreAuthorize("@ss.hasPermi('biz:invoice:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId) {
        return success(InvoiceVO.from(invoiceQueryService.get(orderId)));
    }

    private List<InvoiceVO> listRows(InvoicePageRequest request) {
        return invoiceQueryService.page(new InvoicePageQuery(
                        request.getMerchantId(),
                        request.getOutTradeNo(),
                        request.getInvoiceNo(),
                        request.getInvoiceStatus(),
                        request.getInvoiceIssueTimeBegin(),
                        request.getInvoiceIssueTimeEnd()))
                .stream().map(InvoiceVO::from).collect(Collectors.toList());
    }
}
