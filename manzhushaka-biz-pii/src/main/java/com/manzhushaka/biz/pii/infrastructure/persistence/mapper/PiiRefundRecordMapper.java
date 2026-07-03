package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiRefundRecord;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PiiRefundRecordMapper {
    int insert(PiiRefundRecord entity);
    int updateById(PiiRefundRecord entity);
    int deleteById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("umsTradeNo") String umsTradeNo, @Param("completeTime") LocalDateTime completeTime);
    PiiRefundRecord selectById(@Param("id") Long id);
    PiiRefundRecord selectByOutRefundNo(@Param("outRefundNo") String outRefundNo);
    List<PiiRefundRecord> selectByPayOrderId(@Param("payOrderId") Long payOrderId);
    Long sumSuccessAmountByPayOrderId(@Param("payOrderId") Long payOrderId);
}
