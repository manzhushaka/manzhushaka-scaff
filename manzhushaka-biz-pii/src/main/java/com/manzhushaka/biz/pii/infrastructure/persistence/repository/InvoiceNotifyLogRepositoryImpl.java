package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.InvoiceNotifyLog;
import com.manzhushaka.biz.pii.domain.repository.InvoiceNotifyLogRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.InvoiceNotifyLogConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiInvoiceNotifyLog;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiInvoiceNotifyLogMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InvoiceNotifyLogRepositoryImpl implements InvoiceNotifyLogRepository {
    private final PiiInvoiceNotifyLogMapper mapper;

    public InvoiceNotifyLogRepositoryImpl(PiiInvoiceNotifyLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(InvoiceNotifyLog notifyLog) {
        PiiInvoiceNotifyLog entity = InvoiceNotifyLogConverter.toEntity(notifyLog);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int markProcessed(String umsMerOrderId, String umsMerOrderDate) {
        return mapper.markProcessed(umsMerOrderId, umsMerOrderDate);
    }

    @Override
    public Optional<InvoiceNotifyLog> findByOrder(String umsMerOrderId, String umsMerOrderDate) {
        return Optional.ofNullable(mapper.selectByOrder(umsMerOrderId, umsMerOrderDate)).map(InvoiceNotifyLogConverter::toDomain);
    }
}
