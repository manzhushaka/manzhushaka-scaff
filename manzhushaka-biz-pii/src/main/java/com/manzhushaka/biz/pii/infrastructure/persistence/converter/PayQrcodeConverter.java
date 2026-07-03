package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayQrcode;

public final class PayQrcodeConverter {
    private PayQrcodeConverter() {
    }

    public static PayQrcode toDomain(PiiPayQrcode entity) {
        if (entity == null) {
            return null;
        }
        PayQrcode domain = new PayQrcode();
        domain.setId(entity.getId());
        domain.setMerchantId(entity.getMerchantId());
        domain.setQrcodeCode(entity.getQrcodeCode());
        domain.setQrcodeUrl(entity.getQrcodeUrl());
        domain.setQrcodeImageUrl(entity.getQrcodeImageUrl());
        domain.setName(entity.getName());
        domain.setStatus(entity.getStatus());
        domain.setExpireTime(entity.getExpireTime());
        domain.setRemark(entity.getRemark());
        domain.setCreateTime(entity.getCreateTime());
        domain.setUpdateTime(entity.getUpdateTime());
        domain.setCreateBy(entity.getCreateBy());
        domain.setUpdateBy(entity.getUpdateBy());
        return domain;
    }

    public static PiiPayQrcode toEntity(PayQrcode domain) {
        if (domain == null) {
            return null;
        }
        PiiPayQrcode entity = new PiiPayQrcode();
        entity.setId(domain.getId());
        entity.setMerchantId(domain.getMerchantId());
        entity.setQrcodeCode(domain.getQrcodeCode());
        entity.setQrcodeUrl(domain.getQrcodeUrl());
        entity.setQrcodeImageUrl(domain.getQrcodeImageUrl());
        entity.setName(domain.getName());
        entity.setStatus(domain.getStatus());
        entity.setExpireTime(domain.getExpireTime());
        entity.setRemark(domain.getRemark());
        entity.setCreateTime(domain.getCreateTime());
        entity.setUpdateTime(domain.getUpdateTime());
        entity.setCreateBy(domain.getCreateBy());
        entity.setUpdateBy(domain.getUpdateBy());
        entity.setDelFlag(0);
        return entity;
    }
}
