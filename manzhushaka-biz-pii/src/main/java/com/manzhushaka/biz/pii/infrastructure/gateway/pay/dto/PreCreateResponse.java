package com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto;

public class PreCreateResponse {
    private String prepayId;
    private String jsApiPaySign;
    private String nonceStr;
    private String timestamp;
    private String signType;
    private String packageStr;
    private String payUrl;

    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String prepayId) { this.prepayId = prepayId; }
    public String getJsApiPaySign() { return jsApiPaySign; }
    public void setJsApiPaySign(String jsApiPaySign) { this.jsApiPaySign = jsApiPaySign; }
    public String getNonceStr() { return nonceStr; }
    public void setNonceStr(String nonceStr) { this.nonceStr = nonceStr; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
    public String getPackageStr() { return packageStr; }
    public void setPackageStr(String packageStr) { this.packageStr = packageStr; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
}
