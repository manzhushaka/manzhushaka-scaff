package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateMerchantRequest {
    @NotNull(message = "商户ID不能为空")
    private Long id;
    @NotBlank(message = "商户名称不能为空")
    private String merchantName;
    @NotBlank(message = "银商商户号不能为空")
    private String umsMerchantId;
    @NotBlank(message = "银商终端号不能为空")
    private String umsTerminalId;
    private String umsPaySignKey;
    private String umsInvoiceSignKey;
    private String invoiceMsgSrc;
    private Integer status;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
