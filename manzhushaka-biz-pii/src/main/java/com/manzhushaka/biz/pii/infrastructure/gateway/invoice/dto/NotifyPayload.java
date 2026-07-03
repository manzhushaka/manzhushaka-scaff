package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto;

public class NotifyPayload {
    private String merOrderId;
    private String merOrderDate;
    private String status;
    private String invoiceNo;
    private String invoiceCode;
    private String pdfUrl;

    public String getMerOrderId() { return merOrderId; }
    public void setMerOrderId(String merOrderId) { this.merOrderId = merOrderId; }
    public String getMerOrderDate() { return merOrderDate; }
    public void setMerOrderDate(String merOrderDate) { this.merOrderDate = merOrderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}
