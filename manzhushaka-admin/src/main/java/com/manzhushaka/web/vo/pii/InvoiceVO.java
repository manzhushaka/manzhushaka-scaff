package com.manzhushaka.web.vo.pii;

import com.manzhushaka.biz.pii.application.result.InvoiceResult;

import java.time.LocalDateTime;

public class InvoiceVO {
    private Long id;
    private Long merchantId;
    private String outTradeNo;
    private Long amount;
    private String buyerName;
    private String buyerTaxCode;
    private String buyerEmail;
    private String buyerMobile;
    private String payStatus;
    private LocalDateTime payTime;
    private String invoiceStatus;
    private String invoiceNo;
    private String invoiceCode;
    private String invoicePdfUrl;
    private LocalDateTime invoiceIssueTime;
    private LocalDateTime invoiceReverseTime;
    private LocalDateTime createTime;

    public static InvoiceVO from(InvoiceResult result) {
        InvoiceVO vo = new InvoiceVO();
        vo.setId(result.getId());
        vo.setMerchantId(result.getMerchantId());
        vo.setOutTradeNo(result.getOutTradeNo());
        vo.setAmount(result.getAmount());
        vo.setBuyerName(result.getBuyerName());
        vo.setBuyerTaxCode(result.getBuyerTaxCode());
        vo.setBuyerEmail(result.getBuyerEmail());
        vo.setBuyerMobile(result.getBuyerMobile());
        vo.setPayStatus(result.getPayStatus());
        vo.setPayTime(result.getPayTime());
        vo.setInvoiceStatus(result.getInvoiceStatus());
        vo.setInvoiceNo(result.getInvoiceNo());
        vo.setInvoiceCode(result.getInvoiceCode());
        vo.setInvoicePdfUrl(result.getInvoicePdfUrl());
        vo.setInvoiceIssueTime(result.getInvoiceIssueTime());
        vo.setInvoiceReverseTime(result.getInvoiceReverseTime());
        vo.setCreateTime(result.getCreateTime());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerTaxCode() { return buyerTaxCode; }
    public void setBuyerTaxCode(String buyerTaxCode) { this.buyerTaxCode = buyerTaxCode; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public String getBuyerMobile() { return buyerMobile; }
    public void setBuyerMobile(String buyerMobile) { this.buyerMobile = buyerMobile; }
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getInvoicePdfUrl() { return invoicePdfUrl; }
    public void setInvoicePdfUrl(String invoicePdfUrl) { this.invoicePdfUrl = invoicePdfUrl; }
    public LocalDateTime getInvoiceIssueTime() { return invoiceIssueTime; }
    public void setInvoiceIssueTime(LocalDateTime invoiceIssueTime) { this.invoiceIssueTime = invoiceIssueTime; }
    public LocalDateTime getInvoiceReverseTime() { return invoiceReverseTime; }
    public void setInvoiceReverseTime(LocalDateTime invoiceReverseTime) { this.invoiceReverseTime = invoiceReverseTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
