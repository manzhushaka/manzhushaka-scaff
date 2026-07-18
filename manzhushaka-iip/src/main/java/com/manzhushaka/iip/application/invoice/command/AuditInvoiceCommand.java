package com.manzhushaka.iip.application.invoice.command;

/**
 * 发票审核命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record AuditInvoiceCommand(Long invoiceId, Boolean pass, String auditRemark)
{
    @Override
    public String toString()
    {
        return "AuditInvoiceCommand[invoiceId=" + invoiceId + ", pass=" + pass + "]";
    }
}
