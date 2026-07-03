package com.manzhushaka.biz.pii.application.result;

import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;

public class QrcodeTaxItemResult {
    private Long id;
    private Long qrcodeId;
    private Long taxItemId;
    private Long defaultAmount;

    public static QrcodeTaxItemResult from(PayQrcodeTaxItem relation) {
        QrcodeTaxItemResult result = new QrcodeTaxItemResult();
        result.setId(relation.getId());
        result.setQrcodeId(relation.getQrcodeId());
        result.setTaxItemId(relation.getTaxItemId());
        result.setDefaultAmount(relation.getDefaultAmount());
        return result;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQrcodeId() { return qrcodeId; }
    public void setQrcodeId(Long qrcodeId) { this.qrcodeId = qrcodeId; }
    public Long getTaxItemId() { return taxItemId; }
    public void setTaxItemId(Long taxItemId) { this.taxItemId = taxItemId; }
    public Long getDefaultAmount() { return defaultAmount; }
    public void setDefaultAmount(Long defaultAmount) { this.defaultAmount = defaultAmount; }
}
