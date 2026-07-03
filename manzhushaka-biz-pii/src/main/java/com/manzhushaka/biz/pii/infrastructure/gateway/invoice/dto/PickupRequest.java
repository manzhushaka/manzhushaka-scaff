package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto;

public class PickupRequest {
    private String merchantId;
    private String terminalId;
    private String merOrderId;
    private String merOrderDate;
    private String invoiceNo;
    private String invoiceCode;
    private String msgSrc;
    private String msgId;
    private String signKey;

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getMerOrderId() { return merOrderId; }
    public void setMerOrderId(String merOrderId) { this.merOrderId = merOrderId; }
    public String getMerOrderDate() { return merOrderDate; }
    public void setMerOrderDate(String merOrderDate) { this.merOrderDate = merOrderDate; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getMsgSrc() { return msgSrc; }
    public void setMsgSrc(String msgSrc) { this.msgSrc = msgSrc; }
    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }
    public String getSignKey() { return signKey; }
    public void setSignKey(String signKey) { this.signKey = signKey; }
}
