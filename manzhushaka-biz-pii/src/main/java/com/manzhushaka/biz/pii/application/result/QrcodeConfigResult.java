package com.manzhushaka.biz.pii.application.result;

import java.util.ArrayList;
import java.util.List;

public class QrcodeConfigResult {
    private Long qrcodeId;
    private Long merchantId;
    private String qrcodeCode;
    private String name;
    private String appId;
    private List<QrcodeConfigTaxItemResult> taxItems = new ArrayList<>();

    public Long getQrcodeId() { return qrcodeId; }
    public void setQrcodeId(Long qrcodeId) { this.qrcodeId = qrcodeId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getQrcodeCode() { return qrcodeCode; }
    public void setQrcodeCode(String qrcodeCode) { this.qrcodeCode = qrcodeCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public List<QrcodeConfigTaxItemResult> getTaxItems() { return taxItems; }
    public void setTaxItems(List<QrcodeConfigTaxItemResult> taxItems) { this.taxItems = taxItems; }
}
