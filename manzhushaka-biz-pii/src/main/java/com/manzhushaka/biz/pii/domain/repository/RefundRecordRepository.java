package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.RefundRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefundRecordRepository {
    Long insert(RefundRecord refundRecord);
    int updateById(RefundRecord refundRecord);
    int updateStatus(Long id, String status, String umsTradeNo, LocalDateTime completeTime);
    Optional<RefundRecord> findById(Long id);
    Optional<RefundRecord> findByOutRefundNo(String outRefundNo);
    List<RefundRecord> findByPayOrderId(Long payOrderId);
    Long sumSuccessAmountByPayOrderId(Long payOrderId);
}
