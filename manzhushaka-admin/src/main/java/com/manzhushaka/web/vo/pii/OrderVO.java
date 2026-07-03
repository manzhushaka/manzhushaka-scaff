package com.manzhushaka.web.vo.pii;

import com.manzhushaka.biz.pii.application.result.OrderResult;
import com.manzhushaka.common.annotation.Excel;

import java.time.LocalDateTime;

public class OrderVO {
    @Excel(name = "订单ID")
    private Long id;
    @Excel(name = "商户ID")
    private Long merchantId;
    private Long qrcodeId;
    private Long taxItemId;
    @Excel(name = "订单号", width = 28)
    private String outTradeNo;
    private String umsMerOrderDate;
    @Excel(name = "订单金额(分)")
    private Long amount;
    @Excel(name = "购方名称", width = 24)
    private String buyerName;
    @Excel(name = "购方税号", width = 24)
    private String buyerTaxCode;
    private String buyerEmail;
    private String buyerMobile;
    @Excel(name = "支付状态", readConverterExp = "PENDING=待支付,PAID=已支付,REFUNDING=退款中,REFUNDED=已退款,CLOSED=已关闭")
    private String payStatus;
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    @Excel(name = "支付流水号", width = 24)
    private String payTradeNo;
    @Excel(name = "退款金额(分)")
    private Long refundAmount;
    @Excel(name = "发票状态", readConverterExp = "NONE=未开票,OPENING=开票中,ISSUED=已开票,REVERSING=红冲中,REVERSED=已红冲,FAILED=失败")
    private String invoiceStatus;
    @Excel(name = "发票号码", width = 20)
    private String invoiceNo;
    @Excel(name = "发票代码", width = 20)
    private String invoiceCode;
    private String invoicePdfUrl;
    @Excel(name = "开票时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invoiceIssueTime;
    @Excel(name = "红冲时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invoiceReverseTime;
    private String remark;
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static OrderVO from(OrderResult result) {
        OrderVO vo = new OrderVO();
        vo.setId(result.getId());
        vo.setMerchantId(result.getMerchantId());
        vo.setQrcodeId(result.getQrcodeId());
        vo.setTaxItemId(result.getTaxItemId());
        vo.setOutTradeNo(result.getOutTradeNo());
        vo.setUmsMerOrderDate(result.getUmsMerOrderDate());
        vo.setAmount(result.getAmount());
        vo.setBuyerName(result.getBuyerName());
        vo.setBuyerTaxCode(result.getBuyerTaxCode());
        vo.setBuyerEmail(result.getBuyerEmail());
        vo.setBuyerMobile(result.getBuyerMobile());
        vo.setPayStatus(result.getPayStatus());
        vo.setPayTime(result.getPayTime());
        vo.setPayTradeNo(result.getPayTradeNo());
        vo.setRefundAmount(result.getRefundAmount());
        vo.setInvoiceStatus(result.getInvoiceStatus());
        vo.setInvoiceNo(result.getInvoiceNo());
        vo.setInvoiceCode(result.getInvoiceCode());
        vo.setInvoicePdfUrl(result.getInvoicePdfUrl());
        vo.setInvoiceIssueTime(result.getInvoiceIssueTime());
        vo.setInvoiceReverseTime(result.getInvoiceReverseTime());
        vo.setRemark(result.getRemark());
        vo.setCreateTime(result.getCreateTime());
        vo.setUpdateTime(result.getUpdateTime());
        return vo;
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
