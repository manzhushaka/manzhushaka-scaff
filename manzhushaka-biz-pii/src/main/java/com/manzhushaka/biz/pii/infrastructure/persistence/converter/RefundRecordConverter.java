package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.RefundRecord;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiRefundRecord;

public final class RefundRecordConverter {
    private RefundRecordConverter() {
    }

    public static RefundRecord toDomain(PiiRefundRecord entity) {
        if (entity == null) {
            return null;
        }
        RefundRecord domain = new RefundRecord();
        domain.setId(entity.getId());
        domain.setMerchantId(entity.getMerchantId());
        domain.setPayOrderId(entity.getPayOrderId());
        domain.setOutRefundNo(entity.getOutRefundNo());
        domain.setUmsRefundMerOrderId(entity.getUmsRefundMerOrderId());
        domain.setAmount(entity.getAmount());
        domain.setReason(entity.getReason());
        domain.setStatus(entity.getStatus());
        domain.setUmsTradeNo(entity.getUmsTradeNo());
        domain.setCompleteTime(entity.getCompleteTime());
        domain.setOperatorId(entity.getOperatorId());
        domain.setTriggerInvoiceReverse(entity.getTriggerInvoiceReverse());
        domain.setCreateTime(entity.getCreateTime());
        domain.setUpdateTime(entity.getUpdateTime());
        return domain;
    }

    public static PiiRefundRecord toEntity(RefundRecord domain) {
        if (domain == null) {
            return null;
        }
        PiiRefundRecord entity = new PiiRefundRecord();
        entity.setId(domain.getId());
        entity.setMerchantId(domain.getMerchantId());
        entity.setPayOrderId(domain.getPayOrderId());
        entity.setOutRefundNo(domain.getOutRefundNo());
        entity.setUmsRefundMerOrderId(domain.getUmsRefundMerOrderId());
        entity.setAmount(domain.getAmount());
        entity.setReason(domain.getReason());
        entity.setStatus(domain.getStatus());
        entity.setUmsTradeNo(domain.getUmsTradeNo());
        entity.setCompleteTime(domain.getCompleteTime());
        entity.setOperatorId(domain.getOperatorId());
        entity.setTriggerInvoiceReverse(domain.getTriggerInvoiceReverse());
        entity.setCreateTime(domain.getCreateTime());
        entity.setUpdateTime(domain.getUpdateTime());
        entity.setDelFlag(0);
        return entity;
    }
}
