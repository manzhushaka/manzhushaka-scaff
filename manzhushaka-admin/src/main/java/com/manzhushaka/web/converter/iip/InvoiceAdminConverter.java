package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.invoice.command.AuditInvoiceCommand;
import com.manzhushaka.iip.application.invoice.query.InvoiceQuery;
import com.manzhushaka.web.dto.iip.InvoiceAuditRequest;
import com.manzhushaka.web.dto.iip.InvoiceRequest;

/**
 * 发票管理端 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class InvoiceAdminConverter
{
    private InvoiceAdminConverter()
    {
    }

    /**
     * 查询请求转查询条件。
     *
     * @param request 发票查询请求
     * @return 发票查询条件
     */
    public static InvoiceQuery toQuery(InvoiceRequest request)
    {
        return new InvoiceQuery(request.getStatus(), request.getInvoiceNo(), request.getMerchantName(),
                request.getBeginTime(), request.getEndTime());
    }

    /**
     * 审核请求转审核命令。
     *
     * @param request 发票审核请求
     * @return 发票审核命令
     */
    public static AuditInvoiceCommand toAuditCommand(InvoiceAuditRequest request)
    {
        return new AuditInvoiceCommand(request.getInvoiceId(), request.getPass(), request.getAuditRemark());
    }
}
