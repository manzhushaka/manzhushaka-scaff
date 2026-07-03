package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto;

public class InvoiceResponse {
    private String resultCode;
    private String resultMsg;
    private String status;
    private String invoiceNo;
    private String invoiceCode;
    private String merchantId;
    private String terminalId;
    private String merOrderId;
    private String merOrderDate;
    private String buyerName;
    private Double totalPriceIncludingTax;
    private Double totalTax;
    private Double totalPrice;
    private String pdfUrl;

    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getMerOrderId() { return merOrderId; }
    public void setMerOrderId(String merOrderId) { this.merOrderId = merOrderId; }
    public String getMerOrderDate() { return merOrderDate; }
    public void setMerOrderDate(String merOrderDate) { this.merOrderDate = merOrderDate; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public Double getTotalPriceIncludingTax() { return totalPriceIncludingTax; }
    public void setTotalPriceIncludingTax(Double totalPriceIncludingTax) { this.totalPriceIncludingTax = totalPriceIncludingTax; }
    public Double getTotalTax() { return totalTax; }
    public void setTotalTax(Double totalTax) { this.totalTax = totalTax; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}
