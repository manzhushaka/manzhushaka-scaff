package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateMerchantConfigRequest {
    @NotNull(message = "部门ID不能为空")
    private Long deptId;
    @NotBlank(message = "银商商户号不能为空")
    private String umsMerchantId;
    @NotBlank(message = "银商终端号不能为空")
    private String umsTerminalId;
    private String umsPaySignKey;
    private String umsInvoiceSignKey;
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
    private String remark;

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getUmsMerchantId() { return umsMerchantId; }
    public void setUmsMerchantId(String umsMerchantId) { this.umsMerchantId = umsMerchantId; }
    public String getUmsTerminalId() { return umsTerminalId; }
    public void setUmsTerminalId(String umsTerminalId) { this.umsTerminalId = umsTerminalId; }
    public String getUmsPaySignKey() { return umsPaySignKey; }
    public void setUmsPaySignKey(String umsPaySignKey) { this.umsPaySignKey = umsPaySignKey; }
    public String getUmsInvoiceSignKey() { return umsInvoiceSignKey; }
    public void setUmsInvoiceSignKey(String umsInvoiceSignKey) { this.umsInvoiceSignKey = umsInvoiceSignKey; }
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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
