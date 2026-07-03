package com.manzhushaka.web.dto.pii;

public class MerchantPageRequest {
    private String merchantName;
    private String umsMerchantId;
    private Integer status;

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getUmsMerchantId() { return umsMerchantId; }
    public void setUmsMerchantId(String umsMerchantId) { this.umsMerchantId = umsMerchantId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
