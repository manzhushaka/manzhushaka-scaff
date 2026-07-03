package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiInvoiceCallLog;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PiiInvoiceCallLogMapper {
    int insert(PiiInvoiceCallLog entity);
    int updateById(PiiInvoiceCallLog entity);
    int updateResult(@Param("id") Long id, @Param("responseBody") String responseBody, @Param("success") Integer success, @Param("errorMsg") String errorMsg, @Param("durationMs") Integer durationMs);
    PiiInvoiceCallLog selectByPayOrderIdAndMsgType(@Param("payOrderId") Long payOrderId, @Param("msgType") String msgType);
    List<PiiInvoiceCallLog> selectByPayOrderId(@Param("payOrderId") Long payOrderId);
}
