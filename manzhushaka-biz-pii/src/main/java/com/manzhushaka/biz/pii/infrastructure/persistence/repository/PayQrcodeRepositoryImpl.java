package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.PayQrcodeConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayQrcode;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayQrcodeMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PayQrcodeRepositoryImpl implements PayQrcodeRepository {
    private final PiiPayQrcodeMapper mapper;

    public PayQrcodeRepositoryImpl(PiiPayQrcodeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(PayQrcode qrcode) {
        PiiPayQrcode entity = PayQrcodeConverter.toEntity(qrcode);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int updateById(PayQrcode qrcode) {
        return mapper.updateById(PayQrcodeConverter.toEntity(qrcode));
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return mapper.updateStatus(id, status);
    }

    @Override
    public Optional<PayQrcode> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(PayQrcodeConverter::toDomain);
    }

    @Override
    public Optional<PayQrcode> findByCode(String qrcodeCode) {
        return Optional.ofNullable(mapper.selectByCode(qrcodeCode)).map(PayQrcodeConverter::toDomain);
    }

    @Override
    public List<PayQrcode> findByMerchantId(Long merchantId) {
        return mapper.selectByMerchantId(merchantId).stream().map(PayQrcodeConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<PayQrcode> findExpiredBefore(LocalDateTime time, int limit) {
        return mapper.selectExpiredBefore(time, limit).stream().map(PayQrcodeConverter::toDomain).collect(Collectors.toList());
    }
}
