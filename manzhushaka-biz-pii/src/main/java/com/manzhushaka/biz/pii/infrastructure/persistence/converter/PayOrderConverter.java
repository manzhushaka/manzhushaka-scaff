package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;

public final class PayOrderConverter {
    private PayOrderConverter() {
    }

    public static PayOrder toDomain(PiiPayOrder entity) {
        if (entity == null) {
            return null;
        }
        PayOrder domain = new PayOrder();
        domain.setId(entity.getId());
        domain.setMerchantId(entity.getMerchantId());
        domain.setQrcodeId(entity.getQrcodeId());
        domain.setTaxItemId(entity.getTaxItemId());
        domain.setOutTradeNo(entity.getOutTradeNo());
        domain.setUmsMerOrderDate(entity.getUmsMerOrderDate());
        domain.setAmount(entity.getAmount());
        domain.setBuyerName(entity.getBuyerName());
        domain.setBuyerTaxCode(entity.getBuyerTaxCode());
        domain.setBuyerEmail(entity.getBuyerEmail());
        domain.setBuyerMobile(entity.getBuyerMobile());
        domain.setBuyerOpenid(entity.getBuyerOpenid());
        domain.setPayStatus(entity.getPayStatus());
        domain.setPayTime(entity.getPayTime());
        domain.setPayTradeNo(entity.getPayTradeNo());
        domain.setPayNotifyStatus(entity.getPayNotifyStatus());
        domain.setRefundAmount(entity.getRefundAmount());
        domain.setInvoiceStatus(entity.getInvoiceStatus());
        domain.setInvoiceNo(entity.getInvoiceNo());
        domain.setInvoiceCode(entity.getInvoiceCode());
        domain.setInvoicePdfUrl(entity.getInvoicePdfUrl());
        domain.setInvoiceIssueTime(entity.getInvoiceIssueTime());
        domain.setInvoiceReverseTime(entity.getInvoiceReverseTime());
        domain.setOrderToken(entity.getOrderToken());
        domain.setWechatAppid(entity.getWechatAppid());
        domain.setClientIp(entity.getClientIp());
        domain.setRemark(entity.getRemark());
        domain.setCreateTime(entity.getCreateTime());
        domain.setUpdateTime(entity.getUpdateTime());
        domain.setCreateBy(entity.getCreateBy());
        domain.setUpdateBy(entity.getUpdateBy());
        return domain;
    }

    public static PiiPayOrder toEntity(PayOrder domain) {
        if (domain == null) {
            return null;
        }
        PiiPayOrder entity = new PiiPayOrder();
        entity.setId(domain.getId());
        entity.setMerchantId(domain.getMerchantId());
        entity.setQrcodeId(domain.getQrcodeId());
        entity.setTaxItemId(domain.getTaxItemId());
        entity.setOutTradeNo(domain.getOutTradeNo());
        entity.setUmsMerOrderDate(domain.getUmsMerOrderDate());
        entity.setAmount(domain.getAmount());
        entity.setBuyerName(domain.getBuyerName());
        entity.setBuyerTaxCode(domain.getBuyerTaxCode());
        entity.setBuyerEmail(domain.getBuyerEmail());
        entity.setBuyerMobile(domain.getBuyerMobile());
        entity.setBuyerOpenid(domain.getBuyerOpenid());
        entity.setPayStatus(domain.getPayStatus());
        entity.setPayTime(domain.getPayTime());
        entity.setPayTradeNo(domain.getPayTradeNo());
        entity.setPayNotifyStatus(domain.getPayNotifyStatus());
        entity.setRefundAmount(domain.getRefundAmount());
        entity.setInvoiceStatus(domain.getInvoiceStatus());
        entity.setInvoiceNo(domain.getInvoiceNo());
        entity.setInvoiceCode(domain.getInvoiceCode());
        entity.setInvoicePdfUrl(domain.getInvoicePdfUrl());
        entity.setInvoiceIssueTime(domain.getInvoiceIssueTime());
        entity.setInvoiceReverseTime(domain.getInvoiceReverseTime());
        entity.setOrderToken(domain.getOrderToken());
        entity.setWechatAppid(domain.getWechatAppid());
        entity.setClientIp(domain.getClientIp());
        entity.setRemark(domain.getRemark());
        entity.setCreateTime(domain.getCreateTime());
        entity.setUpdateTime(domain.getUpdateTime());
        entity.setCreateBy(domain.getCreateBy());
        entity.setUpdateBy(domain.getUpdateBy());
        entity.setDelFlag(0);
        return entity;
    }
}
