package com.manzhushaka.iip.application.invoice.command;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户提交发票命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SubmitInvoiceCommand(Long merchantId, String merchantName, String invoiceCode,
        String invoiceNo, Date invoiceDate, BigDecimal amount, String imageUrl)
{
    @Override
    public String toString()
    {
        return "SubmitInvoiceCommand[merchantId=" + merchantId + ", merchantName=" + merchantName
                + ", invoiceNo=" + invoiceNo + ", amount=" + amount + "]";
    }
}
