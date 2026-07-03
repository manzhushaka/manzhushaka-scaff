package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PayOrderRepository {
    Long insert(PayOrder order);
    int updateById(PayOrder order);
    int updatePayStatus(Long id, String payStatus, String payTradeNo, LocalDateTime payTime);
    int updateInvoiceStatus(Long id, String invoiceStatus, String invoiceNo, String invoiceCode,
                            String invoicePdfUrl, LocalDateTime invoiceIssueTime);
    int updateInvoiceReverseStatus(Long id, String invoiceStatus, LocalDateTime invoiceReverseTime);
    int updateRefundAmountAndStatus(Long id, Long refundAmount, String payStatus);
    Optional<PayOrder> findById(Long id);
    Optional<PayOrder> findByOutTradeNo(String outTradeNo);
    Optional<PayOrder> findByOutTradeNoAndToken(String outTradeNo, String orderToken);
    List<PayOrder> findList(Long merchantId, String outTradeNo, String payStatus, String invoiceStatus,
                            LocalDateTime payTimeBegin, LocalDateTime payTimeEnd);
    List<PayOrder> findInvoiceList(Long merchantId, String outTradeNo, String invoiceNo, String invoiceStatus,
                                   LocalDateTime invoiceIssueTimeBegin, LocalDateTime invoiceIssueTimeEnd);
    List<PayOrder> findByMerchantAndStatus(Long merchantId, String payStatus, int limit);
    List<PayOrder> findPendingBefore(LocalDateTime time, int limit);
    long sumAmountByMerchantAndStatusBetween(Long merchantId, List<String> statuses,
                                             LocalDateTime start, LocalDateTime end);
    long countByMerchantAndPayTimeBetween(Long merchantId, LocalDateTime start, LocalDateTime end);
    long sumAmountByInvoiceStatusBetween(Long merchantId, String invoiceStatus, LocalDateTime start, LocalDateTime end);
    long countAbnormalOrders(Long merchantId, LocalDateTime start, LocalDateTime end);
    List<BiDashboardResult.TrendItem> sumAmountByDay(Long merchantId, LocalDateTime start, LocalDateTime end);
    List<BiDashboardResult.TaxItemRatioItem> sumAmountByTaxItem(Long merchantId, LocalDateTime start, LocalDateTime end);
    List<BiDashboardResult.MerchantRankItem> sumAmountByMerchantTop(Long merchantId, int limit, LocalDateTime start, LocalDateTime end);
    List<BiDashboardResult.AbnormalOrderItem> findAbnormalOrders(Long merchantId, LocalDateTime start, LocalDateTime end, int limit);
    long sumAmountByDeptId(Long deptId, LocalDateTime start, LocalDateTime end);
    long countByDeptId(Long deptId, LocalDateTime start, LocalDateTime end);
    long countMerchantsByDeptId(Long deptId);
}
