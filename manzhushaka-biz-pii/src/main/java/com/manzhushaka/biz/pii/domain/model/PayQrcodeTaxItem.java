package com.manzhushaka.biz.pii.domain.model;

public class PayQrcodeTaxItem {
    private Long id;
    private Long qrcodeId;
    private Long taxItemId;
    private Long defaultAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQrcodeId() { return qrcodeId; }
    public void setQrcodeId(Long qrcodeId) { this.qrcodeId = qrcodeId; }
    public Long getTaxItemId() { return taxItemId; }
    public void setTaxItemId(Long taxItemId) { this.taxItemId = taxItemId; }
    public Long getDefaultAmount() { return defaultAmount; }
    public void setDefaultAmount(Long defaultAmount) { this.defaultAmount = defaultAmount; }

    @Override
    public String toString() {
        return "PayQrcodeTaxItem{id=" + id + ", qrcodeId=" + qrcodeId
                + ", taxItemId=" + taxItemId + ", defaultAmount=" + defaultAmount + "}";
    }
}
