package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.InvoiceCallLog;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiInvoiceCallLog;

public final class InvoiceCallLogConverter {
    private InvoiceCallLogConverter() {
    }

    public static InvoiceCallLog toDomain(PiiInvoiceCallLog entity) {
        if (entity == null) {
            return null;
        }
        InvoiceCallLog domain = new InvoiceCallLog();
        domain.setId(entity.getId());
        domain.setPayOrderId(entity.getPayOrderId());
        domain.setMsgType(entity.getMsgType());
        domain.setMsgId(entity.getMsgId());
        domain.setRequestBody(entity.getRequestBody());
        domain.setResponseBody(entity.getResponseBody());
        domain.setDurationMs(entity.getDurationMs());
        domain.setSuccess(entity.getSuccess());
        domain.setErrorMsg(entity.getErrorMsg());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }

    public static PiiInvoiceCallLog toEntity(InvoiceCallLog domain) {
        if (domain == null) {
            return null;
        }
        PiiInvoiceCallLog entity = new PiiInvoiceCallLog();
        entity.setId(domain.getId());
        entity.setPayOrderId(domain.getPayOrderId());
        entity.setMsgType(domain.getMsgType());
        entity.setMsgId(domain.getMsgId());
        entity.setRequestBody(domain.getRequestBody());
        entity.setResponseBody(domain.getResponseBody());
        entity.setDurationMs(domain.getDurationMs());
        entity.setSuccess(domain.getSuccess());
        entity.setErrorMsg(domain.getErrorMsg());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
