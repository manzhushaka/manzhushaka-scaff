package com.manzhushaka.biz.pii.domain.model;

import java.time.LocalDateTime;

public class RefundRecord {
    private Long id;
    private Long merchantId;
    private Long payOrderId;
    private String outRefundNo;
    private String umsRefundMerOrderId;
    private Long amount;
    private String reason;
    private String status;
    private String umsTradeNo;
    private LocalDateTime completeTime;
    private Long operatorId;
    private Integer triggerInvoiceReverse;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getPayOrderId() { return payOrderId; }
    public void setPayOrderId(Long payOrderId) { this.payOrderId = payOrderId; }
    public String getOutRefundNo() { return outRefundNo; }
    public void setOutRefundNo(String outRefundNo) { this.outRefundNo = outRefundNo; }
    public String getUmsRefundMerOrderId() { return umsRefundMerOrderId; }
    public void setUmsRefundMerOrderId(String umsRefundMerOrderId) { this.umsRefundMerOrderId = umsRefundMerOrderId; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUmsTradeNo() { return umsTradeNo; }
    public void setUmsTradeNo(String umsTradeNo) { this.umsTradeNo = umsTradeNo; }
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public Integer getTriggerInvoiceReverse() { return triggerInvoiceReverse; }
    public void setTriggerInvoiceReverse(Integer triggerInvoiceReverse) { this.triggerInvoiceReverse = triggerInvoiceReverse; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return "RefundRecord{id=" + id + ", merchantId=" + merchantId + ", payOrderId="
                + payOrderId + ", outRefundNo='" + outRefundNo + "', amount=" + amount
                + ", status='" + status + "'}";
    }
}
