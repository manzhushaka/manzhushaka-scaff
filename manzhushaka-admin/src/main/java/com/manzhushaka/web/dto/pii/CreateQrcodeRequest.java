package com.manzhushaka.web.dto.pii;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateQrcodeRequest {
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;
    @NotBlank(message = "二维码编码不能为空")
    private String qrcodeCode;
    @NotBlank(message = "二维码名称不能为空")
    private String name;
    private Integer status;
    private LocalDateTime expireTime;
    private String remark;
    @Valid
    private List<QrcodeTaxItemRequest> taxItems = new ArrayList<>();

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getQrcodeCode() { return qrcodeCode; }
    public void setQrcodeCode(String qrcodeCode) { this.qrcodeCode = qrcodeCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<QrcodeTaxItemRequest> getTaxItems() { return taxItems; }
    public void setTaxItems(List<QrcodeTaxItemRequest> taxItems) { this.taxItems = taxItems; }
}
