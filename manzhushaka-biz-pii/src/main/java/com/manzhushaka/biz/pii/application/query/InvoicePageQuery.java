package com.manzhushaka.biz.pii.application.query;

import java.time.LocalDateTime;

public record InvoicePageQuery(Long merchantId, String outTradeNo, String invoiceNo, String invoiceStatus,
                               LocalDateTime invoiceIssueTimeBegin, LocalDateTime invoiceIssueTimeEnd) {
}
