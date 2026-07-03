package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PrecreatePayRequest {
    @NotBlank(message = "二维码编码不能为空")
    private String code;
    @NotNull(message = "税目不能为空")
    private Long taxItemId;
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private Long amount;
    private String buyerName;
    private String buyerTaxCode;
    private String buyerEmail;
    private String buyerMobile;
    private String openid;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getTaxItemId() { return taxItemId; }
    public void setTaxItemId(Long taxItemId) { this.taxItemId = taxItemId; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerTaxCode() { return buyerTaxCode; }
    public void setBuyerTaxCode(String buyerTaxCode) { this.buyerTaxCode = buyerTaxCode; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public String getBuyerMobile() { return buyerMobile; }
    public void setBuyerMobile(String buyerMobile) { this.buyerMobile = buyerMobile; }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
}
