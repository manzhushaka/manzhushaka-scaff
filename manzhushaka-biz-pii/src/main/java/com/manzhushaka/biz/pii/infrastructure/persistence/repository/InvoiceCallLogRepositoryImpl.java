package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.InvoiceCallLog;
import com.manzhushaka.biz.pii.domain.repository.InvoiceCallLogRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.InvoiceCallLogConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiInvoiceCallLog;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiInvoiceCallLogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InvoiceCallLogRepositoryImpl implements InvoiceCallLogRepository {
    private final PiiInvoiceCallLogMapper mapper;

    public InvoiceCallLogRepositoryImpl(PiiInvoiceCallLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(InvoiceCallLog callLog) {
        PiiInvoiceCallLog entity = InvoiceCallLogConverter.toEntity(callLog);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int updateResult(Long id, String responseBody, Integer success, String errorMsg, Integer durationMs) {
        return mapper.updateResult(id, responseBody, success, errorMsg, durationMs);
    }

    @Override
    public Optional<InvoiceCallLog> findByPayOrderIdAndMsgType(Long payOrderId, String msgType) {
        return Optional.ofNullable(mapper.selectByPayOrderIdAndMsgType(payOrderId, msgType)).map(InvoiceCallLogConverter::toDomain);
    }

    @Override
    public List<InvoiceCallLog> findByPayOrderId(Long payOrderId) {
        return mapper.selectByPayOrderId(payOrderId).stream().map(InvoiceCallLogConverter::toDomain).collect(Collectors.toList());
    }
}
