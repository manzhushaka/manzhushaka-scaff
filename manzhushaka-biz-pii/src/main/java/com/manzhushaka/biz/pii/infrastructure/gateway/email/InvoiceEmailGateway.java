package com.manzhushaka.biz.pii.infrastructure.gateway.email;

public interface InvoiceEmailGateway {
    void send(InvoiceEmailRequest request);
}
