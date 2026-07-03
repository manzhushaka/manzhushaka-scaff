package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiMerchantProfile;

public final class MerchantProfileConverter {
    private MerchantProfileConverter() {
    }

    public static MerchantProfile toDomain(PiiMerchantProfile entity) {
        if (entity == null) {
            return null;
        }
        MerchantProfile domain = new MerchantProfile();
        domain.setId(entity.getId());
        domain.setDeptId(entity.getDeptId());
        domain.setMerchantName(entity.getMerchantName());
        domain.setUmsMerchantId(entity.getUmsMerchantId());
        domain.setUmsTerminalId(entity.getUmsTerminalId());
        domain.setUmsPaySignKeyEnc(entity.getUmsPaySignKeyEnc());
        domain.setUmsInvoiceSignKeyEnc(entity.getUmsInvoiceSignKeyEnc());
        domain.setInvoiceMsgSrc(entity.getInvoiceMsgSrc());
        domain.setInvoiceSellerName(entity.getInvoiceSellerName());
        domain.setInvoiceSellerTaxCode(entity.getInvoiceSellerTaxCode());
        domain.setInvoiceSellerAddress(entity.getInvoiceSellerAddress());
        domain.setInvoiceSellerTelephone(entity.getInvoiceSellerTelephone());
        domain.setInvoiceSellerBank(entity.getInvoiceSellerBank());
        domain.setInvoiceSellerAccount(entity.getInvoiceSellerAccount());
        domain.setInvoicePayee(entity.getInvoicePayee());
        domain.setInvoiceChecker(entity.getInvoiceChecker());
        domain.setInvoiceDrawer(entity.getInvoiceDrawer());
        domain.setNotifyUrl(entity.getNotifyUrl());
        domain.setStatus(entity.getStatus());
        domain.setRemark(entity.getRemark());
        domain.setCreateTime(entity.getCreateTime());
        domain.setUpdateTime(entity.getUpdateTime());
        domain.setCreateBy(entity.getCreateBy());
        domain.setUpdateBy(entity.getUpdateBy());
        return domain;
    }

    public static PiiMerchantProfile toEntity(MerchantProfile domain) {
        if (domain == null) {
            return null;
        }
        PiiMerchantProfile entity = new PiiMerchantProfile();
        entity.setId(domain.getId());
        entity.setDeptId(domain.getDeptId());
        entity.setMerchantName(domain.getMerchantName());
        entity.setUmsMerchantId(domain.getUmsMerchantId());
        entity.setUmsTerminalId(domain.getUmsTerminalId());
        entity.setUmsPaySignKeyEnc(domain.getUmsPaySignKeyEnc());
        entity.setUmsInvoiceSignKeyEnc(domain.getUmsInvoiceSignKeyEnc());
        entity.setInvoiceMsgSrc(domain.getInvoiceMsgSrc());
        entity.setInvoiceSellerName(domain.getInvoiceSellerName());
        entity.setInvoiceSellerTaxCode(domain.getInvoiceSellerTaxCode());
        entity.setInvoiceSellerAddress(domain.getInvoiceSellerAddress());
        entity.setInvoiceSellerTelephone(domain.getInvoiceSellerTelephone());
        entity.setInvoiceSellerBank(domain.getInvoiceSellerBank());
        entity.setInvoiceSellerAccount(domain.getInvoiceSellerAccount());
        entity.setInvoicePayee(domain.getInvoicePayee());
        entity.setInvoiceChecker(domain.getInvoiceChecker());
        entity.setInvoiceDrawer(domain.getInvoiceDrawer());
        entity.setNotifyUrl(domain.getNotifyUrl());
        entity.setStatus(domain.getStatus());
        entity.setRemark(domain.getRemark());
        entity.setCreateTime(domain.getCreateTime());
        entity.setUpdateTime(domain.getUpdateTime());
        entity.setCreateBy(domain.getCreateBy());
        entity.setUpdateBy(domain.getUpdateBy());
        return entity;
    }
}
