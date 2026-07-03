package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.PayOrderConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayOrderMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PayOrderRepositoryImpl implements PayOrderRepository {
    private final PiiPayOrderMapper mapper;

    public PayOrderRepositoryImpl(PiiPayOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(PayOrder order) {
        PiiPayOrder entity = PayOrderConverter.toEntity(order);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int updateById(PayOrder order) {
        return mapper.updateById(PayOrderConverter.toEntity(order));
    }

    @Override
    public int updatePayStatus(Long id, String payStatus, String payTradeNo, LocalDateTime payTime) {
        return mapper.updatePayStatus(id, payStatus, payTradeNo, payTime);
    }

    @Override
    public int updateInvoiceStatus(Long id, String invoiceStatus, String invoiceNo, String invoiceCode, String invoicePdfUrl, LocalDateTime invoiceIssueTime) {
        return mapper.updateInvoiceStatus(id, invoiceStatus, invoiceNo, invoiceCode, invoicePdfUrl, invoiceIssueTime);
    }

    @Override
    public int updateInvoiceReverseStatus(Long id, String invoiceStatus, LocalDateTime invoiceReverseTime) {
        return mapper.updateInvoiceReverseStatus(id, invoiceStatus, invoiceReverseTime);
    }

    @Override
    public int updateRefundAmountAndStatus(Long id, Long refundAmount, String payStatus) {
        return mapper.updateRefundAmountAndStatus(id, refundAmount, payStatus);
    }

    @Override
    public Optional<PayOrder> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(PayOrderConverter::toDomain);
    }

    @Override
    public Optional<PayOrder> findByOutTradeNo(String outTradeNo) {
        return Optional.ofNullable(mapper.selectByOutTradeNo(outTradeNo)).map(PayOrderConverter::toDomain);
    }

    @Override
    public Optional<PayOrder> findByOutTradeNoAndToken(String outTradeNo, String orderToken) {
        return Optional.ofNullable(mapper.selectByOutTradeNoAndToken(outTradeNo, orderToken)).map(PayOrderConverter::toDomain);
    }

    @Override
    public List<PayOrder> findList(Long merchantId, String outTradeNo, String payStatus, String invoiceStatus,
                                   LocalDateTime payTimeBegin, LocalDateTime payTimeEnd) {
        return mapper.selectList(merchantId, outTradeNo, payStatus, invoiceStatus, payTimeBegin, payTimeEnd)
                .stream().map(PayOrderConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<PayOrder> findByMerchantAndStatus(Long merchantId, String payStatus, int limit) {
        return mapper.selectByMerchantAndStatus(merchantId, payStatus, limit).stream().map(PayOrderConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<PayOrder> findPendingBefore(LocalDateTime time, int limit) {
        return mapper.selectPendingBefore(time, limit).stream().map(PayOrderConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public long sumAmountByMerchantAndStatusBetween(Long merchantId, List<String> statuses, LocalDateTime start, LocalDateTime end) {
        Long value = mapper.sumAmountByMerchantAndStatusBetween(merchantId, statuses, start, end);
        return value == null ? 0L : value;
    }

    @Override
    public long countByMerchantAndPayTimeBetween(Long merchantId, LocalDateTime start, LocalDateTime end) {
        Long value = mapper.countByMerchantAndPayTimeBetween(merchantId, start, end);
        return value == null ? 0L : value;
    }
}
