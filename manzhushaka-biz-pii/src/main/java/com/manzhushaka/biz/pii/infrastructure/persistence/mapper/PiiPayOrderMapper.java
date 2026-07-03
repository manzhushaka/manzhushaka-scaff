package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PiiPayOrderMapper {
    int insert(PiiPayOrder entity);
    int updateById(PiiPayOrder entity);
    int deleteById(@Param("id") Long id);
    int updatePayStatus(@Param("id") Long id, @Param("payStatus") String payStatus, @Param("payTradeNo") String payTradeNo, @Param("payTime") LocalDateTime payTime);
    int updateInvoiceStatus(@Param("id") Long id, @Param("invoiceStatus") String invoiceStatus, @Param("invoiceNo") String invoiceNo, @Param("invoiceCode") String invoiceCode, @Param("invoicePdfUrl") String invoicePdfUrl, @Param("invoiceIssueTime") LocalDateTime invoiceIssueTime);
    int updateRefundAmountAndStatus(@Param("id") Long id, @Param("refundAmount") Long refundAmount, @Param("payStatus") String payStatus);
    PiiPayOrder selectById(@Param("id") Long id);
    PiiPayOrder selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);
    PiiPayOrder selectByOutTradeNoAndToken(@Param("outTradeNo") String outTradeNo, @Param("orderToken") String orderToken);
    List<PiiPayOrder> selectByMerchantAndStatus(@Param("merchantId") Long merchantId, @Param("payStatus") String payStatus, @Param("limit") int limit);
    List<PiiPayOrder> selectPendingBefore(@Param("time") LocalDateTime time, @Param("limit") int limit);
    Long sumAmountByMerchantAndStatusBetween(@Param("merchantId") Long merchantId, @Param("statuses") List<String> statuses, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    Long countByMerchantAndPayTimeBetween(@Param("merchantId") Long merchantId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
