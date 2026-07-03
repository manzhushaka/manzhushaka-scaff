package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiTaxItem;

public final class TaxItemConverter {
    private TaxItemConverter() {
    }

    public static TaxItem toDomain(PiiTaxItem entity) {
        if (entity == null) {
            return null;
        }
        TaxItem domain = new TaxItem();
        domain.setId(entity.getId());
        domain.setTaxItemCode(entity.getTaxItemCode());
        domain.setName(entity.getName());
        domain.setBrevityCode(entity.getBrevityCode());
        domain.setCategory(entity.getCategory());
        domain.setTaxRate(entity.getTaxRate());
        domain.setVatSpecial(entity.getVatSpecial());
        domain.setFreeTaxType(entity.getFreeTaxType());
        domain.setPreferPolicyFlag(entity.getPreferPolicyFlag());
        domain.setSort(entity.getSort());
        domain.setStatus(entity.getStatus());
        domain.setRemark(entity.getRemark());
        domain.setCreateTime(entity.getCreateTime());
        domain.setUpdateTime(entity.getUpdateTime());
        domain.setCreateBy(entity.getCreateBy());
        domain.setUpdateBy(entity.getUpdateBy());
        return domain;
    }

    public static PiiTaxItem toEntity(TaxItem domain) {
        if (domain == null) {
            return null;
        }
        PiiTaxItem entity = new PiiTaxItem();
        entity.setId(domain.getId());
        entity.setTaxItemCode(domain.getTaxItemCode());
        entity.setName(domain.getName());
        entity.setBrevityCode(domain.getBrevityCode());
        entity.setCategory(domain.getCategory());
        entity.setTaxRate(domain.getTaxRate());
        entity.setVatSpecial(domain.getVatSpecial());
        entity.setFreeTaxType(domain.getFreeTaxType());
        entity.setPreferPolicyFlag(domain.getPreferPolicyFlag());
        entity.setSort(domain.getSort());
        entity.setStatus(domain.getStatus());
        entity.setRemark(domain.getRemark());
        entity.setCreateTime(domain.getCreateTime());
        entity.setUpdateTime(domain.getUpdateTime());
        entity.setCreateBy(domain.getCreateBy());
        entity.setUpdateBy(domain.getUpdateBy());
        entity.setDelFlag(0);
        return entity;
    }
}
