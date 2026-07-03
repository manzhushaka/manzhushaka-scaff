package com.manzhushaka.biz.pii.infrastructure.persistence.entity;

public class PiiPayQrcodeTaxItem {
    private Long id;
    private Long qrcodeId;
    private Long taxItemId;
    private Long defaultAmount;
    private Integer delFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQrcodeId() {
        return qrcodeId;
    }

    public void setQrcodeId(Long qrcodeId) {
        this.qrcodeId = qrcodeId;
    }

    public Long getTaxItemId() {
        return taxItemId;
    }

    public void setTaxItemId(Long taxItemId) {
        this.taxItemId = taxItemId;
    }

    public Long getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(Long defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
