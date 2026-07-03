package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.PaymentNotifyLog;
import com.manzhushaka.biz.pii.domain.repository.PaymentNotifyLogRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.PaymentNotifyLogConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPaymentNotifyLog;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPaymentNotifyLogMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentNotifyLogRepositoryImpl implements PaymentNotifyLogRepository {
    private final PiiPaymentNotifyLogMapper mapper;

    public PaymentNotifyLogRepositoryImpl(PiiPaymentNotifyLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(PaymentNotifyLog notifyLog) {
        PiiPaymentNotifyLog entity = PaymentNotifyLogConverter.toEntity(notifyLog);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int markProcessed(String outTradeNo) {
        return mapper.markProcessed(outTradeNo);
    }

    @Override
    public Optional<PaymentNotifyLog> findByOutTradeNo(String outTradeNo) {
        return Optional.ofNullable(mapper.selectByOutTradeNo(outTradeNo)).map(PaymentNotifyLogConverter::toDomain);
    }
}
