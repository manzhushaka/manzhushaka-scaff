package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.InvoiceNotifyLog;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiInvoiceNotifyLog;

public final class InvoiceNotifyLogConverter {
    private InvoiceNotifyLogConverter() {
    }

    public static InvoiceNotifyLog toDomain(PiiInvoiceNotifyLog entity) {
        if (entity == null) {
            return null;
        }
        InvoiceNotifyLog domain = new InvoiceNotifyLog();
        domain.setId(entity.getId());
        domain.setUmsMerOrderId(entity.getUmsMerOrderId());
        domain.setUmsMerOrderDate(entity.getUmsMerOrderDate());
        domain.setQrcodeId(entity.getQrcodeId());
        domain.setNotifyPayload(entity.getNotifyPayload());
        domain.setSign(entity.getSign());
        domain.setVerifyResult(entity.getVerifyResult());
        domain.setProcessed(entity.getProcessed());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }

    public static PiiInvoiceNotifyLog toEntity(InvoiceNotifyLog domain) {
        if (domain == null) {
            return null;
        }
        PiiInvoiceNotifyLog entity = new PiiInvoiceNotifyLog();
        entity.setId(domain.getId());
        entity.setUmsMerOrderId(domain.getUmsMerOrderId());
        entity.setUmsMerOrderDate(domain.getUmsMerOrderDate());
        entity.setQrcodeId(domain.getQrcodeId());
        entity.setNotifyPayload(domain.getNotifyPayload());
        entity.setSign(domain.getSign());
        entity.setVerifyResult(domain.getVerifyResult());
        entity.setProcessed(domain.getProcessed());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
