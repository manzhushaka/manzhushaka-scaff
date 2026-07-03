package com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto;

public class RefundRequest {
    private String appId;
    private String appKey;
    private String merchantId;
    private String terminalId;
    private String outTradeNo;
    private String refundOrderId;
    private Long refundAmount;
    private String refundDesc;
    private String instMid;
    private boolean prod;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getRefundOrderId() { return refundOrderId; }
    public void setRefundOrderId(String refundOrderId) { this.refundOrderId = refundOrderId; }
    public Long getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Long refundAmount) { this.refundAmount = refundAmount; }
    public String getRefundDesc() { return refundDesc; }
    public void setRefundDesc(String refundDesc) { this.refundDesc = refundDesc; }
    public String getInstMid() { return instMid; }
    public void setInstMid(String instMid) { this.instMid = instMid; }
    public boolean isProd() { return prod; }
    public void setProd(boolean prod) { this.prod = prod; }
}
