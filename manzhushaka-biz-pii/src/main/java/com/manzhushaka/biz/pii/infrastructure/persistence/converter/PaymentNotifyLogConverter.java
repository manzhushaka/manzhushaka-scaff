package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.PaymentNotifyLog;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPaymentNotifyLog;

public final class PaymentNotifyLogConverter {
    private PaymentNotifyLogConverter() {
    }

    public static PaymentNotifyLog toDomain(PiiPaymentNotifyLog entity) {
        if (entity == null) {
            return null;
        }
        PaymentNotifyLog domain = new PaymentNotifyLog();
        domain.setId(entity.getId());
        domain.setOutTradeNo(entity.getOutTradeNo());
        domain.setNotifyPayload(entity.getNotifyPayload());
        domain.setSign(entity.getSign());
        domain.setVerifyResult(entity.getVerifyResult());
        domain.setProcessed(entity.getProcessed());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }

    public static PiiPaymentNotifyLog toEntity(PaymentNotifyLog domain) {
        if (domain == null) {
            return null;
        }
        PiiPaymentNotifyLog entity = new PiiPaymentNotifyLog();
        entity.setId(domain.getId());
        entity.setOutTradeNo(domain.getOutTradeNo());
        entity.setNotifyPayload(domain.getNotifyPayload());
        entity.setSign(domain.getSign());
        entity.setVerifyResult(domain.getVerifyResult());
        entity.setProcessed(domain.getProcessed());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
