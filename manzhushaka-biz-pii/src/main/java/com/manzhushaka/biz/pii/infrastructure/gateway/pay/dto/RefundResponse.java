package com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto;

public class RefundResponse {
    private String errCode;
    private String errMsg;
    private String tradeNo;
    private String refundOrderId;
    private String refundStatus;

    public String getErrCode() { return errCode; }
    public void setErrCode(String errCode) { this.errCode = errCode; }
    public String getErrMsg() { return errMsg; }
    public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
    public String getRefundOrderId() { return refundOrderId; }
    public void setRefundOrderId(String refundOrderId) { this.refundOrderId = refundOrderId; }
    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
}
