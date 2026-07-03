package com.manzhushaka.biz.pii.application.result;

public class PrecreatePayResult {
    private String appId;
    private String outTradeNo;
    private String orderToken;
    private String prepayId;
    private String timeStamp;
    private String nonceStr;
    private String packageStr;
    private String signType;
    private String paySign;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getOrderToken() { return orderToken; }
    public void setOrderToken(String orderToken) { this.orderToken = orderToken; }
    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String prepayId) { this.prepayId = prepayId; }
    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }
    public String getNonceStr() { return nonceStr; }
    public void setNonceStr(String nonceStr) { this.nonceStr = nonceStr; }
    public String getPackageStr() { return packageStr; }
    public void setPackageStr(String packageStr) { this.packageStr = packageStr; }
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
    public String getPaySign() { return paySign; }
    public void setPaySign(String paySign) { this.paySign = paySign; }
}
