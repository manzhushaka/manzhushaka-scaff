package com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto;

public class NotifyPayload {
    private String outTradeNo;
    private String tradeNo;
    private String tradeStatus;
    private String refundOrderId;
    private Long totalAmount;
    private Long refundAmount;

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
    public String getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(String tradeStatus) { this.tradeStatus = tradeStatus; }
    public String getRefundOrderId() { return refundOrderId; }
    public void setRefundOrderId(String refundOrderId) { this.refundOrderId = refundOrderId; }
    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    public Long getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Long refundAmount) { this.refundAmount = refundAmount; }
}
