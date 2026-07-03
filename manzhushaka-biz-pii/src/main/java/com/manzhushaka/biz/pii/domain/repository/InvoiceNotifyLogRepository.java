package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.InvoiceNotifyLog;

import java.util.Optional;

public interface InvoiceNotifyLogRepository {
    Long insert(InvoiceNotifyLog notifyLog);
    int markProcessed(String umsMerOrderId, String umsMerOrderDate);
    Optional<InvoiceNotifyLog> findByOrder(String umsMerOrderId, String umsMerOrderDate);
}
