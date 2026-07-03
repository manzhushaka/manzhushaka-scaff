package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.query.InvoicePageQuery;
import com.manzhushaka.biz.pii.application.service.InvoiceQueryService;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.web.dto.pii.InvoicePageRequest;
import com.manzhushaka.web.vo.pii.InvoiceVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        List<InvoiceVO> rows = invoiceQueryService.page(new InvoicePageQuery(
                        request.getMerchantId(),
                        request.getOutTradeNo(),
                        request.getInvoiceNo(),
                        request.getInvoiceStatus(),
                        request.getInvoiceIssueTimeBegin(),
                        request.getInvoiceIssueTimeEnd()))
                .stream().map(InvoiceVO::from).collect(Collectors.toList());
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('biz:invoice:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId) {
        return success(InvoiceVO.from(invoiceQueryService.get(orderId)));
    }
}
