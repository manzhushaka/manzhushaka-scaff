package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto;

import java.util.UUID;

public class InvoiceRequest {
    private String invoiceMaterial = "ELECTRONIC";
    private String invoiceType = "PLAIN";
    private String merchantId;
    private String terminalId;
    private String merOrderDate;
    private String merOrderId;
    private String buyerName;
    private String buyerTaxCode;
    private String buyerAddress;
    private String buyerTelephone;
    private String buyerBank;
    private String buyerAccount;
    private Long amount;
    private String goodsDetail;
    private String remark;
    private String notifyMobileNo;
    private String notifyEMail;
    private String notifyUrl;
    private String msgSrc;
    private String msgId = UUID.randomUUID().toString();
    private String sign;
    private String signKey;

    public String getInvoiceMaterial() { return invoiceMaterial; }
    public void setInvoiceMaterial(String invoiceMaterial) { this.invoiceMaterial = invoiceMaterial; }
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getMerOrderDate() { return merOrderDate; }
    public void setMerOrderDate(String merOrderDate) { this.merOrderDate = merOrderDate; }
    public String getMerOrderId() { return merOrderId; }
    public void setMerOrderId(String merOrderId) { this.merOrderId = merOrderId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerTaxCode() { return buyerTaxCode; }
    public void setBuyerTaxCode(String buyerTaxCode) { this.buyerTaxCode = buyerTaxCode; }
    public String getBuyerAddress() { return buyerAddress; }
    public void setBuyerAddress(String buyerAddress) { this.buyerAddress = buyerAddress; }
    public String getBuyerTelephone() { return buyerTelephone; }
    public void setBuyerTelephone(String buyerTelephone) { this.buyerTelephone = buyerTelephone; }
    public String getBuyerBank() { return buyerBank; }
    public void setBuyerBank(String buyerBank) { this.buyerBank = buyerBank; }
    public String getBuyerAccount() { return buyerAccount; }
    public void setBuyerAccount(String buyerAccount) { this.buyerAccount = buyerAccount; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getGoodsDetail() { return goodsDetail; }
    public void setGoodsDetail(String goodsDetail) { this.goodsDetail = goodsDetail; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getNotifyMobileNo() { return notifyMobileNo; }
    public void setNotifyMobileNo(String notifyMobileNo) { this.notifyMobileNo = notifyMobileNo; }
    public String getNotifyEMail() { return notifyEMail; }
    public void setNotifyEMail(String notifyEMail) { this.notifyEMail = notifyEMail; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getMsgSrc() { return msgSrc; }
    public void setMsgSrc(String msgSrc) { this.msgSrc = msgSrc; }
    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }
    public String getSign() { return sign; }
    public void setSign(String sign) { this.sign = sign; }
    public String getSignKey() { return signKey; }
    public void setSignKey(String signKey) { this.signKey = signKey; }
}
