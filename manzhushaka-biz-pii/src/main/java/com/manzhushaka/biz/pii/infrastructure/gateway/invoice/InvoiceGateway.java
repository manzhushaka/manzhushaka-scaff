package com.manzhushaka.biz.pii.infrastructure.gateway.invoice;

import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyVerifyResult;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.QueryRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.QueryResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseResponse;

public interface InvoiceGateway {
    InvoiceResponse invoice(InvoiceRequest request);
    ReverseResponse reverse(ReverseRequest request);
    QueryResponse query(QueryRequest request);
    PickupResponse pickup(PickupRequest request);
    NotifyVerifyResult verifyAndParse(String rawBody, String signKey);
}
