package com.manzhushaka.biz.pii.infrastructure.gateway.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingInvoiceEmailGateway implements InvoiceEmailGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingInvoiceEmailGateway.class);

    @Override
    public void send(InvoiceEmailRequest request) {
        log.info("Invoice email send placeholder: payOrderId={}, outTradeNo={}, to={}, invoiceNo={}, pdfUrl={}",
                request.getPayOrderId(), request.getOutTradeNo(), request.getTo(), request.getInvoiceNo(), request.getInvoicePdfUrl());
    }
}
