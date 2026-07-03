package com.manzhushaka.biz.pii.application.result;

import java.math.BigDecimal;

public class QrcodeConfigTaxItemResult {
    private Long id;
    private String taxItemCode;
    private String name;
    private BigDecimal taxRate;
    private Long defaultAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaxItemCode() { return taxItemCode; }
    public void setTaxItemCode(String taxItemCode) { this.taxItemCode = taxItemCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public Long getDefaultAmount() { return defaultAmount; }
    public void setDefaultAmount(Long defaultAmount) { this.defaultAmount = defaultAmount; }
}
