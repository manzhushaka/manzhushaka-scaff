package com.manzhushaka.web.vo.pii;

import com.manzhushaka.biz.pii.application.result.QrcodeTaxItemResult;

public class QrcodeTaxItemVO {
    private Long id;
    private Long qrcodeId;
    private Long taxItemId;
    private Long defaultAmount;

    public static QrcodeTaxItemVO from(QrcodeTaxItemResult result) {
        QrcodeTaxItemVO vo = new QrcodeTaxItemVO();
        vo.setId(result.getId());
        vo.setQrcodeId(result.getQrcodeId());
        vo.setTaxItemId(result.getTaxItemId());
        vo.setDefaultAmount(result.getDefaultAmount());
        return vo;
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
