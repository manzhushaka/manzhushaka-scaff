package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.PayOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PayOrderRepository {
    Long insert(PayOrder order);
    int updateById(PayOrder order);
    int updatePayStatus(Long id, String payStatus, String payTradeNo, LocalDateTime payTime);
    int updateInvoiceStatus(Long id, String invoiceStatus, String invoiceNo, String invoiceCode,
                            String invoicePdfUrl, LocalDateTime invoiceIssueTime);
    int updateRefundAmountAndStatus(Long id, Long refundAmount, String payStatus);
    Optional<PayOrder> findById(Long id);
    Optional<PayOrder> findByOutTradeNo(String outTradeNo);
    Optional<PayOrder> findByOutTradeNoAndToken(String outTradeNo, String orderToken);
    List<PayOrder> findByMerchantAndStatus(Long merchantId, String payStatus, int limit);
    List<PayOrder> findPendingBefore(LocalDateTime time, int limit);
    long sumAmountByMerchantAndStatusBetween(Long merchantId, List<String> statuses,
                                             LocalDateTime start, LocalDateTime end);
    long countByMerchantAndPayTimeBetween(Long merchantId, LocalDateTime start, LocalDateTime end);
}
