package com.manzhushaka.biz.pii.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TaxItem {
    private Long id;
    private String taxItemCode;
    private String name;
    private String brevityCode;
    private String category;
    private BigDecimal taxRate;
    private String vatSpecial;
    private String freeTaxType;
    private String preferPolicyFlag;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }

    @Override
    public String toString() {
        return "TaxItem{id=" + id + ", taxItemCode='" + taxItemCode + "', name='" + name
                + "', taxRate=" + taxRate + ", status=" + status + "}";
    }
}
