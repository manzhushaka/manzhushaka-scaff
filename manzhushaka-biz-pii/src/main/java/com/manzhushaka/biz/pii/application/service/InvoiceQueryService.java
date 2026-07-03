package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.query.InvoicePageQuery;
import com.manzhushaka.biz.pii.application.result.InvoiceResult;

import java.util.List;

public interface InvoiceQueryService {
    List<InvoiceResult> page(InvoicePageQuery query);
    InvoiceResult get(Long orderId);
}
