package com.manzhushaka.biz.pii.application.result;

import com.manzhushaka.biz.pii.domain.model.PayQrcode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QrcodeResult {
    private Long id;
    private Long merchantId;
    private String qrcodeCode;
    private String qrcodeUrl;
    private String qrcodeImageUrl;
    private String name;
    private Integer status;
    private LocalDateTime expireTime;
    private String remark;
    private LocalDateTime createTime;
    private List<QrcodeTaxItemResult> taxItems = new ArrayList<>();

    public static QrcodeResult from(PayQrcode qrcode, List<QrcodeTaxItemResult> taxItems) {
        QrcodeResult result = new QrcodeResult();
        result.setId(qrcode.getId());
        result.setMerchantId(qrcode.getMerchantId());
        result.setQrcodeCode(qrcode.getQrcodeCode());
        result.setQrcodeUrl(qrcode.getQrcodeUrl());
        result.setQrcodeImageUrl(qrcode.getQrcodeImageUrl());
        result.setName(qrcode.getName());
        result.setStatus(qrcode.getStatus());
        result.setExpireTime(qrcode.getExpireTime());
        result.setRemark(qrcode.getRemark());
        result.setCreateTime(qrcode.getCreateTime());
        result.setTaxItems(taxItems);
        return result;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getQrcodeCode() { return qrcodeCode; }
    public void setQrcodeCode(String qrcodeCode) { this.qrcodeCode = qrcodeCode; }
    public String getQrcodeUrl() { return qrcodeUrl; }
    public void setQrcodeUrl(String qrcodeUrl) { this.qrcodeUrl = qrcodeUrl; }
    public String getQrcodeImageUrl() { return qrcodeImageUrl; }
    public void setQrcodeImageUrl(String qrcodeImageUrl) { this.qrcodeImageUrl = qrcodeImageUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public List<QrcodeTaxItemResult> getTaxItems() { return taxItems; }
    public void setTaxItems(List<QrcodeTaxItemResult> taxItems) { this.taxItems = taxItems; }
}
