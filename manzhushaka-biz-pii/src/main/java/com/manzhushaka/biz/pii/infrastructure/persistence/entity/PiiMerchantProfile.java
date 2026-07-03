package com.manzhushaka.biz.pii.infrastructure.persistence.entity;

import java.time.LocalDateTime;

public class PiiMerchantProfile {
    private Long id;
    private Long deptId;
    private String merchantName;
    private String umsMerchantId;
    private String umsTerminalId;
    private String umsPaySignKeyEnc;
    private String umsInvoiceSignKeyEnc;
    private String invoiceMsgSrc;
    private String invoiceSellerName;
    private String invoiceSellerTaxCode;
    private String invoiceSellerAddress;
    private String invoiceSellerTelephone;
    private String invoiceSellerBank;
    private String invoiceSellerAccount;
    private String invoicePayee;
    private String invoiceChecker;
    private String invoiceDrawer;
    private String notifyUrl;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private Integer delFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getUmsMerchantId() {
        return umsMerchantId;
    }

    public void setUmsMerchantId(String umsMerchantId) {
        this.umsMerchantId = umsMerchantId;
    }

    public String getUmsTerminalId() {
        return umsTerminalId;
    }

    public void setUmsTerminalId(String umsTerminalId) {
        this.umsTerminalId = umsTerminalId;
    }

    public String getUmsPaySignKeyEnc() {
        return umsPaySignKeyEnc;
    }

    public void setUmsPaySignKeyEnc(String umsPaySignKeyEnc) {
        this.umsPaySignKeyEnc = umsPaySignKeyEnc;
    }

    public String getUmsInvoiceSignKeyEnc() {
        return umsInvoiceSignKeyEnc;
    }

    public void setUmsInvoiceSignKeyEnc(String umsInvoiceSignKeyEnc) {
        this.umsInvoiceSignKeyEnc = umsInvoiceSignKeyEnc;
    }

    public String getInvoiceMsgSrc() {
        return invoiceMsgSrc;
    }

    public void setInvoiceMsgSrc(String invoiceMsgSrc) {
        this.invoiceMsgSrc = invoiceMsgSrc;
    }

    public String getInvoiceSellerName() {
        return invoiceSellerName;
    }

    public void setInvoiceSellerName(String invoiceSellerName) {
        this.invoiceSellerName = invoiceSellerName;
    }

    public String getInvoiceSellerTaxCode() {
        return invoiceSellerTaxCode;
    }

    public void setInvoiceSellerTaxCode(String invoiceSellerTaxCode) {
        this.invoiceSellerTaxCode = invoiceSellerTaxCode;
    }

    public String getInvoiceSellerAddress() {
        return invoiceSellerAddress;
    }

    public void setInvoiceSellerAddress(String invoiceSellerAddress) {
        this.invoiceSellerAddress = invoiceSellerAddress;
    }

    public String getInvoiceSellerTelephone() {
        return invoiceSellerTelephone;
    }

    public void setInvoiceSellerTelephone(String invoiceSellerTelephone) {
        this.invoiceSellerTelephone = invoiceSellerTelephone;
    }

    public String getInvoiceSellerBank() {
        return invoiceSellerBank;
    }

    public void setInvoiceSellerBank(String invoiceSellerBank) {
        this.invoiceSellerBank = invoiceSellerBank;
    }

    public String getInvoiceSellerAccount() {
        return invoiceSellerAccount;
    }

    public void setInvoiceSellerAccount(String invoiceSellerAccount) {
        this.invoiceSellerAccount = invoiceSellerAccount;
    }

    public String getInvoicePayee() {
        return invoicePayee;
    }

    public void setInvoicePayee(String invoicePayee) {
        this.invoicePayee = invoicePayee;
    }

    public String getInvoiceChecker() {
        return invoiceChecker;
    }

    public void setInvoiceChecker(String invoiceChecker) {
        this.invoiceChecker = invoiceChecker;
    }

    public String getInvoiceDrawer() {
        return invoiceDrawer;
    }

    public void setInvoiceDrawer(String invoiceDrawer) {
        this.invoiceDrawer = invoiceDrawer;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
