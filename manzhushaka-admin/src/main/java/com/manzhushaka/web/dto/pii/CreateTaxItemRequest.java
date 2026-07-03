package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateTaxItemRequest {
    @NotBlank(message = "税目编码不能为空")
    private String taxItemCode;
    @NotBlank(message = "税目名称不能为空")
    private String name;
    private String brevityCode;
    private String category;
    @NotNull(message = "税率不能为空")
    private BigDecimal taxRate;
    private String vatSpecial;
    private String freeTaxType;
    private String preferPolicyFlag;
    private Integer sort;
    private Integer status;
    private String remark;

    public String getTaxItemCode() { return taxItemCode; }
    public void setTaxItemCode(String taxItemCode) { this.taxItemCode = taxItemCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrevityCode() { return brevityCode; }
    public void setBrevityCode(String brevityCode) { this.brevityCode = brevityCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public String getVatSpecial() { return vatSpecial; }
    public void setVatSpecial(String vatSpecial) { this.vatSpecial = vatSpecial; }
    public String getFreeTaxType() { return freeTaxType; }
    public void setFreeTaxType(String freeTaxType) { this.freeTaxType = freeTaxType; }
    public String getPreferPolicyFlag() { return preferPolicyFlag; }
    public void setPreferPolicyFlag(String preferPolicyFlag) { this.preferPolicyFlag = preferPolicyFlag; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
