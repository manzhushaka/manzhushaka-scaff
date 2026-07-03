package com.manzhushaka.biz.pii.domain.model;

import java.time.LocalDateTime;

public class InvoiceNotifyLog {
    private Long id;
    private String umsMerOrderId;
    private String umsMerOrderDate;
    private String qrcodeId;
    private String notifyPayload;
    private String sign;
    private Integer verifyResult;
    private Integer processed;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUmsMerOrderId() { return umsMerOrderId; }
    public void setUmsMerOrderId(String umsMerOrderId) { this.umsMerOrderId = umsMerOrderId; }
    public String getUmsMerOrderDate() { return umsMerOrderDate; }
    public void setUmsMerOrderDate(String umsMerOrderDate) { this.umsMerOrderDate = umsMerOrderDate; }
    public String getQrcodeId() { return qrcodeId; }
    public void setQrcodeId(String qrcodeId) { this.qrcodeId = qrcodeId; }
    public String getNotifyPayload() { return notifyPayload; }
    public void setNotifyPayload(String notifyPayload) { this.notifyPayload = notifyPayload; }
    public String getSign() { return sign; }
    public void setSign(String sign) { this.sign = sign; }
    public Integer getVerifyResult() { return verifyResult; }
    public void setVerifyResult(Integer verifyResult) { this.verifyResult = verifyResult; }
    public Integer getProcessed() { return processed; }
    public void setProcessed(Integer processed) { this.processed = processed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "InvoiceNotifyLog{id=" + id + ", umsMerOrderId='" + umsMerOrderId
                + "', umsMerOrderDate='" + umsMerOrderDate + "', verifyResult="
                + verifyResult + ", processed=" + processed + "}";
    }
}
