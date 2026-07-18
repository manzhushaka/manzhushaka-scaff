package com.manzhushaka.iip.application.invoice.query;

/**
 * 发票查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record InvoiceQuery(String status, String invoiceNo, String merchantName,
        String beginTime, String endTime)
{
}
