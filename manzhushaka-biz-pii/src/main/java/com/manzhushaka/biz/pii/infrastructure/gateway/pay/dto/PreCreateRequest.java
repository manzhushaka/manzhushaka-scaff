package com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto;

public class PreCreateRequest {
    private String appId;
    private String appKey;
    private String merchantId;
    private String terminalId;
    private String outTradeNo;
    private String merOrderDate;
    private Long totalAmount;
    private String openid;
    private String notifyUrl;
    private String signKey;
    private String orderDesc;
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
    public String getMerOrderDate() { return merOrderDate; }
    public void setMerOrderDate(String merOrderDate) { this.merOrderDate = merOrderDate; }
    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getSignKey() { return signKey; }
    public void setSignKey(String signKey) { this.signKey = signKey; }
    public String getOrderDesc() { return orderDesc; }
    public void setOrderDesc(String orderDesc) { this.orderDesc = orderDesc; }
    public String getInstMid() { return instMid; }
    public void setInstMid(String instMid) { this.instMid = instMid; }
    public boolean isProd() { return prod; }
    public void setProd(boolean prod) { this.prod = prod; }
}
