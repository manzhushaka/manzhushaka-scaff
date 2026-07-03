package com.manzhushaka.web.dto.pii;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class InvoicePageRequest {
    private Long merchantId;
    private String outTradeNo;
    private String invoiceNo;
    private String invoiceStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invoiceIssueTimeBegin;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invoiceIssueTimeEnd;

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public LocalDateTime getInvoiceIssueTimeBegin() { return invoiceIssueTimeBegin; }
    public void setInvoiceIssueTimeBegin(LocalDateTime invoiceIssueTimeBegin) { this.invoiceIssueTimeBegin = invoiceIssueTimeBegin; }
    public LocalDateTime getInvoiceIssueTimeEnd() { return invoiceIssueTimeEnd; }
    public void setInvoiceIssueTimeEnd(LocalDateTime invoiceIssueTimeEnd) { this.invoiceIssueTimeEnd = invoiceIssueTimeEnd; }
}
