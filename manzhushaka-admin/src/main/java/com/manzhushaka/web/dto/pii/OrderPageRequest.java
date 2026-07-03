package com.manzhushaka.web.dto.pii;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class OrderPageRequest {
    private Long merchantId;
    private String outTradeNo;
    private String payStatus;
    private String invoiceStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTimeBegin;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTimeEnd;

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public LocalDateTime getPayTimeBegin() { return payTimeBegin; }
    public void setPayTimeBegin(LocalDateTime payTimeBegin) { this.payTimeBegin = payTimeBegin; }
    public LocalDateTime getPayTimeEnd() { return payTimeEnd; }
    public void setPayTimeEnd(LocalDateTime payTimeEnd) { this.payTimeEnd = payTimeEnd; }
}
