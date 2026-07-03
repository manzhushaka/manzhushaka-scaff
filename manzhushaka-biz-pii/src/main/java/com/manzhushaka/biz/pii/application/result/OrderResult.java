package com.manzhushaka.biz.pii.application.result;

import com.manzhushaka.biz.pii.domain.model.PayOrder;

import java.time.LocalDateTime;

public class OrderResult {
    private Long id;
    private Long merchantId;
    private Long qrcodeId;
    private Long taxItemId;
    private String outTradeNo;
    private String umsMerOrderDate;
    private Long amount;
    private String buyerName;
    private String buyerTaxCode;
    private String buyerEmail;
    private String buyerMobile;
    private String payStatus;
    private LocalDateTime payTime;
    private String payTradeNo;
    private Long refundAmount;
    private String invoiceStatus;
    private String invoiceNo;
    private String invoiceCode;
    private String invoicePdfUrl;
    private LocalDateTime invoiceIssueTime;
    private LocalDateTime invoiceReverseTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static OrderResult from(PayOrder order) {
        OrderResult result = new OrderResult();
        result.setId(order.getId());
        result.setMerchantId(order.getMerchantId());
        result.setQrcodeId(order.getQrcodeId());
        result.setTaxItemId(order.getTaxItemId());
        result.setOutTradeNo(order.getOutTradeNo());
        result.setUmsMerOrderDate(order.getUmsMerOrderDate());
        result.setAmount(order.getAmount());
        result.setBuyerName(order.getBuyerName());
        result.setBuyerTaxCode(order.getBuyerTaxCode());
        result.setBuyerEmail(order.getBuyerEmail());
        result.setBuyerMobile(order.getBuyerMobile());
        result.setPayStatus(order.getPayStatus());
        result.setPayTime(order.getPayTime());
        result.setPayTradeNo(order.getPayTradeNo());
        result.setRefundAmount(order.getRefundAmount());
        result.setInvoiceStatus(order.getInvoiceStatus());
        result.setInvoiceNo(order.getInvoiceNo());
        result.setInvoiceCode(order.getInvoiceCode());
        result.setInvoicePdfUrl(order.getInvoicePdfUrl());
        result.setInvoiceIssueTime(order.getInvoiceIssueTime());
        result.setInvoiceReverseTime(order.getInvoiceReverseTime());
        result.setRemark(order.getRemark());
        result.setCreateTime(order.getCreateTime());
        result.setUpdateTime(order.getUpdateTime());
        return result;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getQrcodeId() { return qrcodeId; }
    public void setQrcodeId(Long qrcodeId) { this.qrcodeId = qrcodeId; }
    public Long getTaxItemId() { return taxItemId; }
    public void setTaxItemId(Long taxItemId) { this.taxItemId = taxItemId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getUmsMerOrderDate() { return umsMerOrderDate; }
    public void setUmsMerOrderDate(String umsMerOrderDate) { this.umsMerOrderDate = umsMerOrderDate; }
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
    public String getPayTradeNo() { return payTradeNo; }
    public void setPayTradeNo(String payTradeNo) { this.payTradeNo = payTradeNo; }
    public Long getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Long refundAmount) { this.refundAmount = refundAmount; }
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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
