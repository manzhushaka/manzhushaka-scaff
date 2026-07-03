package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayQrcodeTaxItem;

public final class PayQrcodeTaxItemConverter {
    private PayQrcodeTaxItemConverter() {
    }

    public static PayQrcodeTaxItem toDomain(PiiPayQrcodeTaxItem entity) {
        if (entity == null) {
            return null;
        }
        PayQrcodeTaxItem domain = new PayQrcodeTaxItem();
        domain.setId(entity.getId());
        domain.setQrcodeId(entity.getQrcodeId());
        domain.setTaxItemId(entity.getTaxItemId());
        domain.setDefaultAmount(entity.getDefaultAmount());
        return domain;
    }

    public static PiiPayQrcodeTaxItem toEntity(PayQrcodeTaxItem domain) {
        if (domain == null) {
            return null;
        }
        PiiPayQrcodeTaxItem entity = new PiiPayQrcodeTaxItem();
        entity.setId(domain.getId());
        entity.setQrcodeId(domain.getQrcodeId());
        entity.setTaxItemId(domain.getTaxItemId());
        entity.setDefaultAmount(domain.getDefaultAmount());
        return entity;
    }
}
