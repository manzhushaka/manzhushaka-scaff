package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeTaxItemRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.PayQrcodeTaxItemConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayQrcodeTaxItem;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayQrcodeTaxItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PayQrcodeTaxItemRepositoryImpl implements PayQrcodeTaxItemRepository {
    private final PiiPayQrcodeTaxItemMapper mapper;

    public PayQrcodeTaxItemRepositoryImpl(PiiPayQrcodeTaxItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(PayQrcodeTaxItem relation) {
        PiiPayQrcodeTaxItem entity = PayQrcodeTaxItemConverter.toEntity(relation);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int deleteByQrcodeId(Long qrcodeId) {
        return mapper.deleteByQrcodeId(qrcodeId);
    }

    @Override
    public int deleteByQrcodeIdAndTaxItemId(Long qrcodeId, Long taxItemId) {
        return mapper.deleteByQrcodeIdAndTaxItemId(qrcodeId, taxItemId);
    }

    @Override
    public Optional<PayQrcodeTaxItem> findByQrcodeIdAndTaxItemId(Long qrcodeId, Long taxItemId) {
        return Optional.ofNullable(mapper.selectByQrcodeIdAndTaxItemId(qrcodeId, taxItemId)).map(PayQrcodeTaxItemConverter::toDomain);
    }

    @Override
    public List<PayQrcodeTaxItem> findByQrcodeId(Long qrcodeId) {
        return mapper.selectByQrcodeId(qrcodeId).stream().map(PayQrcodeTaxItemConverter::toDomain).collect(Collectors.toList());
    }
}
