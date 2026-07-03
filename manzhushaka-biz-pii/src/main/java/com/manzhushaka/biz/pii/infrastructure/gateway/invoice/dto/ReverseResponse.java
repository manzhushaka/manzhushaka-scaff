package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto;

public class ReverseResponse {
    private String resultCode;
    private String resultMsg;
    private String status;
    private String merchantId;
    private String terminalId;
    private String merOrderId;
    private String merOrderDate;

    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getMerOrderId() { return merOrderId; }
    public void setMerOrderId(String merOrderId) { this.merOrderId = merOrderId; }
    public String getMerOrderDate() { return merOrderDate; }
    public void setMerOrderDate(String merOrderDate) { this.merOrderDate = merOrderDate; }
}
