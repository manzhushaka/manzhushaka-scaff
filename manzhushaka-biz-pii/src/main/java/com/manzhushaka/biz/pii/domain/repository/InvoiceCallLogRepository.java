package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.InvoiceCallLog;

import java.util.List;
import java.util.Optional;

public interface InvoiceCallLogRepository {
    Long insert(InvoiceCallLog callLog);
    int updateResult(Long id, String responseBody, Integer success, String errorMsg, Integer durationMs);
    Optional<InvoiceCallLog> findByPayOrderIdAndMsgType(Long payOrderId, String msgType);
    List<InvoiceCallLog> findByPayOrderId(Long payOrderId);
}
