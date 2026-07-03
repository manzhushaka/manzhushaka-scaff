package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PiiPayOrderMapper {
    int insert(PiiPayOrder entity);
    int updateById(PiiPayOrder entity);
    int deleteById(@Param("id") Long id);
    int updatePayStatus(@Param("id") Long id, @Param("payStatus") String payStatus, @Param("payTradeNo") String payTradeNo, @Param("payTime") LocalDateTime payTime);
    int updateInvoiceStatus(@Param("id") Long id, @Param("invoiceStatus") String invoiceStatus, @Param("invoiceNo") String invoiceNo, @Param("invoiceCode") String invoiceCode, @Param("invoicePdfUrl") String invoicePdfUrl, @Param("invoiceIssueTime") LocalDateTime invoiceIssueTime);
    int updateInvoiceReverseStatus(@Param("id") Long id, @Param("invoiceStatus") String invoiceStatus, @Param("invoiceReverseTime") LocalDateTime invoiceReverseTime);
    int updateRefundAmountAndStatus(@Param("id") Long id, @Param("refundAmount") Long refundAmount, @Param("payStatus") String payStatus);
    PiiPayOrder selectById(@Param("id") Long id);
    PiiPayOrder selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);
    PiiPayOrder selectByOutTradeNoAndToken(@Param("outTradeNo") String outTradeNo, @Param("orderToken") String orderToken);
    List<PiiPayOrder> selectList(@Param("merchantId") Long merchantId, @Param("outTradeNo") String outTradeNo,
                                 @Param("payStatus") String payStatus, @Param("invoiceStatus") String invoiceStatus,
                                 @Param("payTimeBegin") LocalDateTime payTimeBegin,
                                 @Param("payTimeEnd") LocalDateTime payTimeEnd);
    List<PiiPayOrder> selectInvoiceList(@Param("merchantId") Long merchantId, @Param("outTradeNo") String outTradeNo,
                                        @Param("invoiceNo") String invoiceNo,
                                        @Param("invoiceStatus") String invoiceStatus,
                                        @Param("invoiceIssueTimeBegin") LocalDateTime invoiceIssueTimeBegin,
                                        @Param("invoiceIssueTimeEnd") LocalDateTime invoiceIssueTimeEnd);
    List<PiiPayOrder> selectByMerchantAndStatus(@Param("merchantId") Long merchantId, @Param("payStatus") String payStatus, @Param("limit") int limit);
    List<PiiPayOrder> selectPendingBefore(@Param("time") LocalDateTime time, @Param("limit") int limit);
    Long sumAmountByMerchantAndStatusBetween(@Param("merchantId") Long merchantId, @Param("statuses") List<String> statuses, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Long countByMerchantAndPayTimeBetween(@Param("merchantId") Long merchantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Long sumAmountByInvoiceStatusBetween(@Param("merchantId") Long merchantId, @Param("invoiceStatus") String invoiceStatus, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Long countAbnormalOrders(@Param("merchantId") Long merchantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    List<BiDashboardResult.TrendItem> sumAmountByDay(@Param("merchantId") Long merchantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    List<BiDashboardResult.TaxItemRatioItem> sumAmountByTaxItem(@Param("merchantId") Long merchantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    List<BiDashboardResult.MerchantRankItem> sumAmountByMerchantTop(@Param("merchantId") Long merchantId, @Param("limit") int limit, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    List<BiDashboardResult.AbnormalOrderItem> findAbnormalOrders(@Param("merchantId") Long merchantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("limit") int limit);
    Long sumAmountByDeptId(@Param("deptId") Long deptId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Long countByDeptId(@Param("deptId") Long deptId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Long countMerchantsByDeptId(@Param("deptId") Long deptId);
}
