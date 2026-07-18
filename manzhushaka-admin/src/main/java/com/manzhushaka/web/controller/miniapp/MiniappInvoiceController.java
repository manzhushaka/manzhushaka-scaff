package com.manzhushaka.web.controller.miniapp;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.invoice.result.InvoiceResult;
import com.manzhushaka.iip.application.invoice.service.InvoiceAppService;
import com.manzhushaka.web.converter.iip.InvoiceMiniappConverter;
import com.manzhushaka.web.dto.miniapp.InvoiceSubmitRequest;

/**
 * 小程序发票 信息操作处理
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/miniapp/invoice")
public class MiniappInvoiceController extends BaseController
{
    @Autowired
    private InvoiceAppService invoiceAppService;

    /**
     * 当前用户提交发票
     */
    @PostMapping("/submit")
    public AjaxResult submit(@Validated @RequestBody InvoiceSubmitRequest request)
    {
        Long memberId = SecurityContextHelper.getUserId();
        return success(invoiceAppService.submitInvoice(InvoiceMiniappConverter.toSubmitCommand(request), memberId));
    }

    /**
     * 获取当前用户的发票列表（状态可选，按创建时间倒序）
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "status", required = false) String status)
    {
        List<InvoiceResult> list = invoiceAppService.listMemberInvoices(SecurityContextHelper.getUserId(), status);
        return success(list);
    }

    /**
     * 获取当前用户的发票详情（仅本人可查）
     */
    @GetMapping(value = "/{invoiceId}")
    public AjaxResult getInfo(@PathVariable Long invoiceId)
    {
        return success(invoiceAppService.getMemberInvoice(SecurityContextHelper.getUserId(), invoiceId));
    }
}
