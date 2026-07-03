package com.manzhushaka.biz.pii.infrastructure.gateway.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(InvoiceEmailGateway.class)
public class LoggingInvoiceEmailGateway implements InvoiceEmailGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingInvoiceEmailGateway.class);

    @Override
    public void send(InvoiceEmailRequest request) {
        log.info("Invoice email send placeholder: payOrderId={}, outTradeNo={}, to={}, invoiceNo={}, pdfUrl={}",
                request.getPayOrderId(), request.getOutTradeNo(), request.getTo(), request.getInvoiceNo(), request.getInvoicePdfUrl());
    }
}
