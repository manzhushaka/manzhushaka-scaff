package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.RefundRecord;
import com.manzhushaka.biz.pii.domain.repository.RefundRecordRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.RefundRecordConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiRefundRecord;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiRefundRecordMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RefundRecordRepositoryImpl implements RefundRecordRepository {
    private final PiiRefundRecordMapper mapper;

    public RefundRecordRepositoryImpl(PiiRefundRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(RefundRecord refundRecord) {
        PiiRefundRecord entity = RefundRecordConverter.toEntity(refundRecord);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int updateById(RefundRecord refundRecord) {
        return mapper.updateById(RefundRecordConverter.toEntity(refundRecord));
    }

    @Override
    public int updateStatus(Long id, String status, String umsTradeNo, LocalDateTime completeTime) {
        return mapper.updateStatus(id, status, umsTradeNo, completeTime);
    }

    @Override
    public Optional<RefundRecord> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(RefundRecordConverter::toDomain);
    }

    @Override
    public Optional<RefundRecord> findByOutRefundNo(String outRefundNo) {
        return Optional.ofNullable(mapper.selectByOutRefundNo(outRefundNo)).map(RefundRecordConverter::toDomain);
    }

    @Override
    public List<RefundRecord> findByPayOrderId(Long payOrderId) {
        return mapper.selectByPayOrderId(payOrderId).stream().map(RefundRecordConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public Long sumSuccessAmountByPayOrderId(Long payOrderId) {
        Long value = mapper.sumSuccessAmountByPayOrderId(payOrderId);
        return value == null ? 0L : value;
    }
}
