package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.result.AnonInvoiceDownloadResult;
import com.manzhushaka.biz.pii.application.result.AnonOrderResult;

public interface AnonOrderService {
    AnonOrderResult getOrder(String outTradeNo, String token);
    AnonInvoiceDownloadResult downloadInvoice(String outTradeNo, String token);
}
