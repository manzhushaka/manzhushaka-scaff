package com.manzhushaka.web.vo.pii;

import com.manzhushaka.biz.pii.application.result.MerchantConfigResult;

import java.time.LocalDateTime;

public class MerchantConfigVO {
    private Long id;
    private Long deptId;
    private String merchantName;
    private String umsMerchantId;
    private String umsTerminalId;
    private String umsPaySignKeyMasked;
    private String umsInvoiceSignKeyMasked;
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
    private LocalDateTime updateTime;

    public static MerchantConfigVO from(MerchantConfigResult result) {
        MerchantConfigVO vo = new MerchantConfigVO();
        vo.setId(result.getId());
        vo.setDeptId(result.getDeptId());
        vo.setMerchantName(result.getMerchantName());
        vo.setUmsMerchantId(result.getUmsMerchantId());
        vo.setUmsTerminalId(result.getUmsTerminalId());
        vo.setUmsPaySignKeyMasked(result.getUmsPaySignKeyMasked());
        vo.setUmsInvoiceSignKeyMasked(result.getUmsInvoiceSignKeyMasked());
        vo.setInvoiceMsgSrc(result.getInvoiceMsgSrc());
        vo.setInvoiceSellerName(result.getInvoiceSellerName());
        vo.setInvoiceSellerTaxCode(result.getInvoiceSellerTaxCode());
        vo.setInvoiceSellerAddress(result.getInvoiceSellerAddress());
        vo.setInvoiceSellerTelephone(result.getInvoiceSellerTelephone());
        vo.setInvoiceSellerBank(result.getInvoiceSellerBank());
        vo.setInvoiceSellerAccount(result.getInvoiceSellerAccount());
        vo.setInvoicePayee(result.getInvoicePayee());
        vo.setInvoiceChecker(result.getInvoiceChecker());
        vo.setInvoiceDrawer(result.getInvoiceDrawer());
        vo.setNotifyUrl(result.getNotifyUrl());
        vo.setStatus(result.getStatus());
        vo.setRemark(result.getRemark());
        vo.setUpdateTime(result.getUpdateTime());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getUmsMerchantId() { return umsMerchantId; }
    public void setUmsMerchantId(String umsMerchantId) { this.umsMerchantId = umsMerchantId; }
    public String getUmsTerminalId() { return umsTerminalId; }
    public void setUmsTerminalId(String umsTerminalId) { this.umsTerminalId = umsTerminalId; }
    public String getUmsPaySignKeyMasked() { return umsPaySignKeyMasked; }
    public void setUmsPaySignKeyMasked(String umsPaySignKeyMasked) { this.umsPaySignKeyMasked = umsPaySignKeyMasked; }
    public String getUmsInvoiceSignKeyMasked() { return umsInvoiceSignKeyMasked; }
    public void setUmsInvoiceSignKeyMasked(String umsInvoiceSignKeyMasked) { this.umsInvoiceSignKeyMasked = umsInvoiceSignKeyMasked; }
    public String getInvoiceMsgSrc() { return invoiceMsgSrc; }
    public void setInvoiceMsgSrc(String invoiceMsgSrc) { this.invoiceMsgSrc = invoiceMsgSrc; }
    public String getInvoiceSellerName() { return invoiceSellerName; }
    public void setInvoiceSellerName(String invoiceSellerName) { this.invoiceSellerName = invoiceSellerName; }
    public String getInvoiceSellerTaxCode() { return invoiceSellerTaxCode; }
    public void setInvoiceSellerTaxCode(String invoiceSellerTaxCode) { this.invoiceSellerTaxCode = invoiceSellerTaxCode; }
    public String getInvoiceSellerAddress() { return invoiceSellerAddress; }
    public void setInvoiceSellerAddress(String invoiceSellerAddress) { this.invoiceSellerAddress = invoiceSellerAddress; }
    public String getInvoiceSellerTelephone() { return invoiceSellerTelephone; }
    public void setInvoiceSellerTelephone(String invoiceSellerTelephone) { this.invoiceSellerTelephone = invoiceSellerTelephone; }
    public String getInvoiceSellerBank() { return invoiceSellerBank; }
    public void setInvoiceSellerBank(String invoiceSellerBank) { this.invoiceSellerBank = invoiceSellerBank; }
    public String getInvoiceSellerAccount() { return invoiceSellerAccount; }
    public void setInvoiceSellerAccount(String invoiceSellerAccount) { this.invoiceSellerAccount = invoiceSellerAccount; }
    public String getInvoicePayee() { return invoicePayee; }
    public void setInvoicePayee(String invoicePayee) { this.invoicePayee = invoicePayee; }
    public String getInvoiceChecker() { return invoiceChecker; }
    public void setInvoiceChecker(String invoiceChecker) { this.invoiceChecker = invoiceChecker; }
    public String getInvoiceDrawer() { return invoiceDrawer; }
    public void setInvoiceDrawer(String invoiceDrawer) { this.invoiceDrawer = invoiceDrawer; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
