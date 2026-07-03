package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.PaymentNotifyLog;

import java.util.Optional;

public interface PaymentNotifyLogRepository {
    Long insert(PaymentNotifyLog notifyLog);
    int markProcessed(String outTradeNo);
    Optional<PaymentNotifyLog> findByOutTradeNo(String outTradeNo);
}
