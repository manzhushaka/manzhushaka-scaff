package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiInvoiceNotifyLog;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

public interface PiiInvoiceNotifyLogMapper {
    int insert(PiiInvoiceNotifyLog entity);
    int updateById(PiiInvoiceNotifyLog entity);
    int markProcessed(@Param("umsMerOrderId") String umsMerOrderId, @Param("umsMerOrderDate") String umsMerOrderDate);
    PiiInvoiceNotifyLog selectByOrder(@Param("umsMerOrderId") String umsMerOrderId, @Param("umsMerOrderDate") String umsMerOrderDate);
}
