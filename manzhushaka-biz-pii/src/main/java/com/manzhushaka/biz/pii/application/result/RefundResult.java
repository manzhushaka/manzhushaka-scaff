package com.manzhushaka.biz.pii.application.result;

public class RefundResult {
    private Long id;
    private String outRefundNo;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOutRefundNo() { return outRefundNo; }
    public void setOutRefundNo(String outRefundNo) { this.outRefundNo = outRefundNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
