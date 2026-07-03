package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPaymentNotifyLog;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

public interface PiiPaymentNotifyLogMapper {
    int insert(PiiPaymentNotifyLog entity);
    int updateById(PiiPaymentNotifyLog entity);
    int markProcessed(@Param("outTradeNo") String outTradeNo);
    PiiPaymentNotifyLog selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);
}
