package com.manzhushaka.web.dto.pii;

public class TaxItemPageRequest {
    private String taxItemCode;
    private String name;
    private Integer status;

    public String getTaxItemCode() { return taxItemCode; }
    public void setTaxItemCode(String taxItemCode) { this.taxItemCode = taxItemCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
